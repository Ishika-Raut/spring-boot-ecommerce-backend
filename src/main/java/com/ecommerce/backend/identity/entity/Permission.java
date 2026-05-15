//package com.ecommerce.backend.identity.entity;
//
//package com.ishika.ecommerce.identity.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "permissions")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Permission {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long permissionId;
//
//    //Enum
//    @Column(nullable = false, unique = true, length = 100)
//    private String permissionName;   // e.g., PRODUCT_CREATE_OWN, SELLER_APPROVE
//
//    @Column(length = 500)
//    private String description;
//
//    //Enum
//    @Column(length = 50)
//    private String module;           // CATALOG, ORDER, USER, SELLER, ADMIN, etc.
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//}