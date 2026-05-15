package com.ecommerce.backend.identity.repository;

import com.ecommerce.backend.enums.RoleEnum;
import com.ecommerce.backend.identity.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleEnum name);
}