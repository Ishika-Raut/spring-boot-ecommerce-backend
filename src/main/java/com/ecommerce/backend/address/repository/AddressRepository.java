package com.ecommerce.backend.address.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.backend.address.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> 
{
	List<Address> findByUserUserId(Long userId);

	@Modifying
	@Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userId = :userId")
	void clearDefaultForUser(@Param("userId") Long userId);

	Optional<Address> findByAddressIdAndUserUserId(Long addressId, Long userId);
	
    //Optional<Role> findByName(RoleEnum name);
}