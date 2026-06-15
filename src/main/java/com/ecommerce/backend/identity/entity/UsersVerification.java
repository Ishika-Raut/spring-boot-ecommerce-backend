package com.ecommerce.backend.identity.entity;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.ecommerce.backend.enums.CodeType;
import com.ecommerce.backend.enums.VerificationChannel;
import com.ecommerce.backend.enums.VerificationStatus;
import com.ecommerce.backend.enums.VerificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class UsersVerification 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false, length = 50)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private VerificationChannel channel;

    @Column(name = "target", nullable = false, length = 150)
    private String target;                // Email or Phone number

    @Column(name = "code", nullable = false, length = 255)
    private String code;                  // OTP or Token actual

    @Enumerated(EnumType.STRING)
    @Column(name = "code_type", nullable = false, length = 20)
    private CodeType codeType;			  // OTP or Token Enum

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Builder.Default
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus = VerificationStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // === Helper Methods ===

    public boolean isExpired() {
    	//LocalDateTime.now(). - current date + current time
    	//this.expiryTime - token ka expiration time
    	//isAfter() - kya current time expiry time ke baad ha
        return LocalDateTime.now().isAfter(this.expiryTime); 
    }

    public boolean isValid() {
    	return this.verificationStatus == VerificationStatus.ACTIVE && !isExpired() && this.attempts < 5;
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}