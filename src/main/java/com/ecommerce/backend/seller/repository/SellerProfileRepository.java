package com.ecommerce.backend.seller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.backend.enums.SellerStatus;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.seller.entity.SellerProfile;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> 
{
    Optional<SellerProfile> findByUser(User user);

    List<SellerProfile> findBySellerStatus( SellerStatus status);

    boolean existsByGstin(String gstin);

    boolean existsByPan(String pan);
}