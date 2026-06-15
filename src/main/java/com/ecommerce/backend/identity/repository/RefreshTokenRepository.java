package com.ecommerce.backend.identity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.identity.entity.RefreshToken;
import com.ecommerce.backend.identity.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String token);
    
    Optional<RefreshToken> findByRefreshTokenAndRevokedFalse(String refreshToken);

	List<RefreshToken> findAllByUserAndRevokedFalse(User user);
}