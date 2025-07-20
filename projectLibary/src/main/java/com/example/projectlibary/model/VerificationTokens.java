package com.example.projectlibary.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "verification_tokens")
public class VerificationTokens {
    // Thời gian sống của token tính bằng phút (ví dụ: 24 giờ)
    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;
    // FetchType.EAGER: Khi tải VerificationToken, tự động tải luôn thông tin User
    // Điều này hữu ích vì chúng ta luôn cần User khi xác thực token.
    // orphanRemoval = true: Nếu VerificationToken bị xóa, không ảnh hưởng đến User.
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "fk_verificationtokens_user"))
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    public VerificationTokens(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }


    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }

}
