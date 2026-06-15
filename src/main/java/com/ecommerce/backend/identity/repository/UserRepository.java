package com.ecommerce.backend.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecommerce.backend.identity.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);
	
	boolean existsByPhone(String phone);

	Optional<User> findByPhone(String phone);

	@Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.userId = :userId")
	Optional<User> findByIdWithRoles(Long userId);
}
