package com.ecommerce.backend.identity.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

	//Why Long tokenId instead of UUID?
	//For refresh tokens, a simple auto-increment Long PK is preferred for performance and easier management.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    //actual jwt token string
    @Column(name = "refresh_token", nullable = false, unique = true, length = 512)
    private String refreshToken;

    //One user can have multiple refresh tokens (multiple devices).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

//    @CreationTimestamp => (Hibernate Specific) 
//    Automatically sets the timestamp when the entity is first persisted (INSERT). No need to write manual methods.
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist // JPA Standard — works with any JPA provider.
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiryDate == null) {
            this.expiryDate = LocalDateTime.now().plusDays(7); // default 7 days
        }
    }

    // Helper method - Very useful
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    // Helper method
    public boolean isValid() {
        return !revoked && !isExpired();
    }
}