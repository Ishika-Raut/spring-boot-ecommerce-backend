package com.ecommerce.backend.identity.entity;
	
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecommerce.backend.enums.UserStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails{

    @Id
    //MySQL AUTO_INCREMENT - maintains: last generated value
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false, nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Builder.Default
    //@Builder will ignore the initializing expression entirely. If you want the initializing expression to serve
    //as default, add @Builder.Default. If it is not supposed to be settable during building, make the field final.
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Builder.Default
    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;
    
    //User entity implements UserDetails and during Spring Security authentication: getAuthorities() is called.
    //Inside: roles.stream() is used. If session is closed roles will be loaded lazily
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    
    @Builder.Default
    //Use Set - to avoid duplicates roles entries for same User
    private Set<Role> roles = new HashSet<>();
    
    @Builder.Default 
    //cascade = CascadeType.ALL → If you delete a User, all their RefreshTokens are automatically deleted.
    //orphanRemoval = true → If you remove a token from the list, it gets deleted from database automatically.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    //Hibernate/JPA lifecycle callback automatically runs before save and before update.
    
    @PrePersist //BEFORE INSERT - Automatically timestamps will set
    //No manually required to set: user.setCreatedAt(...)
    public void prePersist() {
    	System.out.println("perPersist User entity");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = UserStatus.INACTIVE;
        }
    }
    
    @PreUpdate //BEFORE UPDATE - Whenever user updates then Automatically: updatedAt = current time
    public void preUpdate() {
    	System.out.println("perUpdate User entity");
        this.updatedAt = LocalDateTime.now();
    }
    
    //to handle multiple roles
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
    	System.out.println("getAuthorities User entity");
    	 return roles.stream()
    	            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name() ) )
    	            .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }
    
}
