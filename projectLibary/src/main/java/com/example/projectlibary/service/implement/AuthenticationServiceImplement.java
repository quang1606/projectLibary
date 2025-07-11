package com.example.projectlibary.service.implement;

import com.example.projectlibary.configuration.SecurityConfig;
import com.example.projectlibary.dto.reponse.LoginResponse;
import com.example.projectlibary.dto.reponse.RefreshTokenResponse;
import com.example.projectlibary.dto.request.LoginRequest;
import com.example.projectlibary.model.CustomUserDetails;
import com.example.projectlibary.model.RefreshToken;
import com.example.projectlibary.service.AuthenticationService;
import com.example.projectlibary.service.RefreshTokenService;
import com.example.projectlibary.service.TokenBlacklistService;
import com.example.projectlibary.utils.JwtTokenUtil;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImplement implements AuthenticationService {
    private final  AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.refresh-token.cookie-name}")
    private String refreshTokenCookieName;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;
    @Override
    public LoginResponse login(HttpServletResponse response, LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser().getEmail());
        addRefreshTokenCookie(response, refreshToken.getToken());
        return new LoginResponse(accessToken,userDetails.getUser().getEmail(),userDetails.getUser().getRole());
    }

    @Override
    public void logout(HttpServletResponse response, HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            long remainingExpiration = jwtTokenUtil.getRemainingExpiration(accessToken);
            if(remainingExpiration > 0) {
                tokenBlacklistService.blacklist(accessToken,remainingExpiration);
            }
        }
        // 2. Vô hiệu hóa Refresh Token (xóa khỏi DB và xóa cookie)
        // Đọc token từ cookie thay vì từ SecurityContext
        Optional.ofNullable(getRefreshTokenFromCookie(request)).ifPresent(token -> {
            refreshTokenService.deleteByToken(token);
        });
        // *** NEW ***: Gửi response để xóa cookie ở client
        deleteRefreshTokenCookie(response);

        SecurityContextHolder.clearContext();
    }


    // === CHANGED ===: Chỉ nhận chuỗi token làm tham số
    public RefreshTokenResponse refreshToken(String refreshToken) throws BadRequestException {
        return refreshTokenService.finByToken(refreshToken)
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtTokenUtil.generateAccessToken((UserDetails) user);
                    // Không cần trả lại refresh token trong body
                    return new RefreshTokenResponse(accessToken);
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database or invalid!"));
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, null); // Đặt giá trị là null
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: Đổi thành TRUE khi deploy
        cookie.setPath("/");
        cookie.setMaxAge(0); // Hết hạn ngay lập tức -> trình duyệt sẽ xóa
        response.addCookie(cookie);
    }

    private String  getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(refreshTokenCookieName, token);
        cookie.setHttpOnly(true); // Ngăn JavaScript truy cập -> Chống XSS
        cookie.setSecure(false); // TODO: Đổi thành TRUE khi deploy lên môi trường production (HTTPS)
        cookie.setPath("/"); // Áp dụng cho toàn bộ domain
        cookie.setMaxAge((int) (refreshTokenDurationMs / 1000)); // Thời gian sống của cookie tính bằng giây
        response.addCookie(cookie);
    }
}
