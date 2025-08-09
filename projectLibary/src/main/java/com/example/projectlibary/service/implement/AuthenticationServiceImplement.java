package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.UserRole;
import com.example.projectlibary.dto.reponse.LoginResponse;
import com.example.projectlibary.dto.reponse.RefreshTokenResponse;
import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.request.ForgotPasswordRequest;
import com.example.projectlibary.dto.request.LoginRequest;
import com.example.projectlibary.dto.request.RegistrationRequest;
import com.example.projectlibary.dto.request.RestPasswordRequest;
import com.example.projectlibary.event.ForgotPasswordEvent;
import com.example.projectlibary.event.UserRegistrationEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.model.CustomUserDetails;
import com.example.projectlibary.model.RefreshToken;
import com.example.projectlibary.model.User;
import com.example.projectlibary.model.VerificationTokens;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.repository.VerificationTokensRepository;
import com.example.projectlibary.service.AuthenticationService;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import com.example.projectlibary.service.RefreshTokenService;
import com.example.projectlibary.service.TokenBlacklistService;
import com.example.projectlibary.utils.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImplement implements AuthenticationService {
    private final  AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;
    private final VerificationTokensRepository verificationTokensRepository;
    private final PasswordEncoder passwordEncoder;

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

        Optional.ofNullable(getRefreshTokenFromCookie(request)).ifPresent(token -> {
            refreshTokenService.deleteByToken(token);
        });

        deleteRefreshTokenCookie(response);

        SecurityContextHolder.clearContext();
    }



    public RefreshTokenResponse refreshToken(String refreshToken) throws BadRequestException {
        return refreshTokenService.finByToken(refreshToken)
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = new CustomUserDetails(user);


                    String accessToken = jwtTokenUtil.generateAccessToken(userDetails);

                    return new RefreshTokenResponse(accessToken);
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database or invalid!"));
    }

    @Override
    @Transactional
    public UserResponse register(RegistrationRequest registrationRequest, HttpServletRequest  request) {
        if(userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXIST);
        }
        if(userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXIST);
        }
        User newUser = new User();
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setUsername(registrationRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setRole(UserRole.STUDENT);
        newUser.setActive(false);
        User saveUser= userRepository.save(newUser);
        log.info("Successfully registered user with email: {}", saveUser.getEmail());
        try {
            String appUrl = getAppUrl(request);
            UserRegistrationEvent event = UserRegistrationEvent.builder()
                    .event("REGISTER")
                    .userId(saveUser.getId())
                    .email(saveUser.getEmail())
                    .appUrl(appUrl)
                    .build();
            kafkaProducerService.sendUserRegistrationEvent(event);
        } catch (Exception e) {
            log.error("Error sending registration event to Kafka for user {}: {}", saveUser.getEmail(), e.getMessage());
        }
        return null;
    }

    @Override
    public void createVerificationTokenForUser(User user, String token) {
        VerificationTokens myToken = new VerificationTokens(token,user);
        verificationTokensRepository.save(myToken);
    }

    @Override
    @Transactional
    public String validateVerificationToken(String token) {
        VerificationTokens verificationTokens = verificationTokensRepository.findByToken(token)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        User user = verificationTokens.getUser();

        if (verificationTokens.isExpired()) {
            verificationTokensRepository.delete(verificationTokens);
            throw new AppException(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }

        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .event("VERIFY")
                .userId(user.getId())
                .email(user.getEmail())
                .build();
        kafkaProducerService.sendUserRegistrationEvent(event);

        user.setActive(true);
        userRepository.save(user);
        verificationTokensRepository.delete(verificationTokens); // Xóa token sau khi đã sử dụng
        return "valid";
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(forgotPasswordRequest.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String appUrl = getAppUrl(request);
            ForgotPasswordEvent event = ForgotPasswordEvent.builder()
                    .Email(forgotPasswordRequest.getEmail())
                    .UserId(user.getId())
                    .appUrl(appUrl)
                    .build();
            kafkaProducerService.sendForgotPasswordEvent(event);
        }
    }

    @Override
    @Transactional
    public void restPassword(RestPasswordRequest restPasswordRequest,String token) {
        VerificationTokens verificationTokens = verificationTokensRepository.findByToken(token).orElseThrow(()->new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN));
        if(verificationTokens.isExpired()) {
            verificationTokensRepository.delete(verificationTokens);
            throw new AppException(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }

        User user = verificationTokens.getUser();
        user.setPassword(passwordEncoder.encode(restPasswordRequest.getPassword()));
        userRepository.save(user);
        verificationTokensRepository.delete(verificationTokens);
    }

    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: Đổi thành TRUE khi deploy
        cookie.setPath("/");
        cookie.setMaxAge(0);
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
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: Đổi thành TRUE khi deploy lên môi trường production (HTTPS)
        cookie.setPath("/"); // Áp dụng cho toàn bộ domain
        cookie.setMaxAge((int) (refreshTokenDurationMs / 1000)); // Thời gian sống của cookie tính bằng giây
        response.addCookie(cookie);
    }
}
