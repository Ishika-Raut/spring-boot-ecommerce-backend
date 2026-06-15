package com.ecommerce.backend.seller.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ecommerce.backend.enums.SellerStatus;
import com.ecommerce.backend.identity.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "seller_profile")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
public class SellerProfile 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long sellerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;                    // Optional bidirectional relationship

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(name = "gstin", unique = true, length = 15)
    private String gstin;

    @Column(name = "pan", unique = true, length = 10)
    private String pan;

    @Column(name = "bank_account_number", length = 30)
    private String bankAccountNumber;

    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "seller_status", nullable = false, length = 30)
    private SellerStatus sellerStatus = SellerStatus.PENDING;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}