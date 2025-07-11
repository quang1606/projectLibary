package com.example.projectlibary.utils;

import com.example.projectlibary.model.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {
    // Lấy các giá trị từ file application.properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    /**
     * Tạo Access Token từ thông tin UserDetails.
     * @param userDetails Đối tượng chứa thông tin người dùng được Spring Security quản lý.
     * @return Chuỗi JWT Access Token.
     */
    public String generateAccessToken(UserDetails userDetails) {
        // Tạo các "claims" (thông tin thêm) cho token
        Map<String, Object> claims = new HashMap<>();
        // Thêm danh sách quyền (roles) vào claims
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Gọi hàm chung để tạo token
        return doGenerateToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    /**
     * Hàm chung để tạo ra một chuỗi JWT.
     * @param claims Các thông tin thêm.
     * @param subject Định danh của người dùng (thường là email hoặc username).
     * @param expirationTime Thời gian sống của token (tính bằng mili giây).
     * @return Chuỗi JWT.
     */
    private String doGenerateToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims) // Đặt các claims tùy chỉnh
                .setSubject(subject) // Đặt chủ thể của token
                .setIssuedAt(new Date(System.currentTimeMillis())) // Đặt thời gian phát hành
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Đặt thời gian hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Ký token với thuật toán HS256
                .compact(); // Xây dựng và trả về chuỗi token
    }

    /**
     * Xác thực token có hợp lệ hay không.
     * @param token Chuỗi JWT.
     * @param userDetails Thông tin người dùng để đối chiếu.
     * @return true nếu token hợp lệ, ngược lại false.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        // Kiểm tra username trong token có khớp với UserDetails và token chưa hết hạn
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Các hàm helper để trích xuất thông tin từ token

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Lấy thời gian sống còn lại của token (tính bằng mili giây).
     * Dùng cho việc blacklist token khi logout.
     * @param token Chuỗi JWT.
     * @return Thời gian còn lại.
     */
    public long getRemainingExpiration(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.getTime() - System.currentTimeMillis();
    }


}
