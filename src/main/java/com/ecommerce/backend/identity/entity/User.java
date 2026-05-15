	package com.ecommerce.backend.identity.entity;
	
	import java.time.LocalDateTime;
	import java.util.HashSet;
	import java.util.Set;
	import java.util.UUID;
	
	import com.ecommerce.backend.enums.UserStatus;
	
	import jakarta.persistence.Column;
	import jakarta.persistence.Entity;
	import jakarta.persistence.EnumType;
	import jakarta.persistence.Enumerated;
	import jakarta.persistence.FetchType;
	import jakarta.persistence.GeneratedValue;
	import jakarta.persistence.GenerationType;
	import jakarta.persistence.Id;
	import jakarta.persistence.JoinTable;
	import jakarta.persistence.ManyToMany;
	import jakarta.persistence.PrePersist;
	import jakarta.persistence.PreUpdate;
	import jakarta.persistence.Table;
	import jakarta.persistence.JoinColumn;
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
	public class User {
	
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
	
	    @Column(name = "email_verified", nullable = false)
	    private Boolean emailVerified = false;
	
	    @Column(name = "phone_verified", nullable = false)
	    private Boolean phoneVerified = false;
	    
	    //Whenever User is fetched, roles will ALSO be fetched immediately.
	    @ManyToMany(fetch = FetchType.EAGER)
	    @JoinTable(
	            name = "user_roles",
	            joinColumns = @JoinColumn(name = "user_id"),
	            inverseJoinColumns = @JoinColumn(name = "role_id")
	    )
	    
	    //Use Set - to avoid duplicates roles entries for same User
	    private Set<Role> roles = new HashSet<>();
	
	    @Column(name = "created_at", nullable = false, updatable = false)
	    private LocalDateTime createdAt;
	
	    @Column(name = "updated_at")
	    private LocalDateTime updatedAt;
	
	    @Column(name = "last_login_at")
	    private LocalDateTime lastLoginAt;
	
	    //Hibernate/JPA lifecycle callbacks automatically runs before save and before update.
	    
	    @PrePersist //BEFORE INSERT - Automatically timestamps will set
	    //No manually required to set: user.setCreatedAt(...)
	    public void prePersist() {
	        this.createdAt = LocalDateTime.now();
	        this.updatedAt = LocalDateTime.now();
	
	        if (this.status == null) {
	            this.status = UserStatus.INACTIVE;
	        }
	    }
	    
	    @PreUpdate //BEFORE UPDATE - Whenever user updates then Automatically: updatedAt = current time
	    public void preUpdate() {
	        this.updatedAt = LocalDateTime.now();
	    }
	}
