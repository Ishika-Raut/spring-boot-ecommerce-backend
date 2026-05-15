//package com.ecommerce.backend.identity.entity;
//
//
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "visitor_sessions")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class GuestUser {
//
//    @Id
//    private String sessionId;           // UUID as String
//
//    @Column(length = 45)
//    private String ipAddress;
//
//    @Column(columnDefinition = "TEXT")
//    private String userAgent;
//
//    @Column(length = 100)
//    private String source;
//
//    @Column(name = "logged_in_user_id")
//    private String loggedInUserId;      // UUID as String (nullable)
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(nullable = false)
//    private LocalDateTime lastActivity;
//
//    @Column(nullable = false)
//    private LocalDateTime expiryTime;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private SessionStatus status;
//
//    public enum SessionStatus {
//        ACTIVE, EXPIRED, LOGGED_IN, ABANDONED
//    }
//}