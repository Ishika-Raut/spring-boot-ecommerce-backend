// This Is Standard RBAC Setup

package com.ecommerce.backend.common.config;

import com.ecommerce.backend.enums.RoleEnum;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.identity.entity.Role;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.repository.RoleRepository;
import com.ecommerce.backend.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component  //tells SC: its an bean
@RequiredArgsConstructor
//CommandLineRunner - Spring Boot interface runs code automatically after application starts
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.name}")
	private String adminName;

	@Value("${app.admin.email}")
	private String adminEmail;

	@Value("${app.admin.password}")
	private String adminPassword;

	@Value("${app.admin.phone}")
	private String adminPhone;

    @Override
    @Transactional
    //run() method automatically called and executes everytime when appn starts 
    //it is different from main method - which starts appn
    public void run(String... args) {

        seedRoles();

        seedAdminUser();
    }

    private void seedRoles() {

    	//condition to check - if 1st startup means no rows or 0 rows in roles tables - then only save roles
        if (roleRepository.count() == 0) {

            List<Role> roles = List.of(

                Role.builder()
                        .name(RoleEnum.USER)
                        .description("Regular customer who can browse and purchase")
                        .build(),

                Role.builder()
                        .name(RoleEnum.SELLER)
                        .description("Seller who can list and sell products")
                        .build(),

                Role.builder()
                        .name(RoleEnum.ADMIN)
                        .description("Platform administrator with full access")
                        .build()
            );

            roleRepository.saveAll(roles);

            System.out.println("Default roles seeded successfully!");
        }
    }

    private void seedAdminUser() {

    	//check admin already exist in db with this email or not - to avoid duplicate admin create
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

        	//From db Role table getting ADMIN role object so that we can assign role
            Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            User admin = User.builder() //create user object
            		.name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .phone(adminPhone)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .phoneVerified(true)
                    .build();
            //admin is user object and user has field: Set<Roles> roles
            //adminRole is Role object
            //Set<Roles> roles setting the role from adminRole
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);

            System.out.println("Super Admin created successfully!");
            System.out.println("Email: admin@ecommerce.com");
            System.out.println("Password: Admin@12345");
        }
    }
}