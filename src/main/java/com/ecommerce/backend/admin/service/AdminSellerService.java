package com.ecommerce.backend.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.backend.enums.RoleEnum;
import com.ecommerce.backend.enums.SellerStatus;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.identity.entity.Role;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.repository.RoleRepository;
import com.ecommerce.backend.identity.repository.UserRepository;
import com.ecommerce.backend.seller.dto.SellerStatusUpdateRequestDTO;
import com.ecommerce.backend.seller.entity.SellerProfile;
import com.ecommerce.backend.seller.repository.SellerProfileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSellerService {

    private final SellerProfileRepository sellerProfileRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public List<SellerProfile> getPendingApplications()
    {
        return sellerProfileRepository.findBySellerStatus(SellerStatus.PENDING);
    }

    public List<SellerProfile> getApprovedSellers()
    {
        return sellerProfileRepository.findBySellerStatus(SellerStatus.APPROVED);
    }

    public List<SellerProfile> getRejectedApplications()
    {
        return sellerProfileRepository.findBySellerStatus(SellerStatus.REJECTED);
    }

    @Transactional
    public void updateSellerStatus( Long sellerId, SellerStatusUpdateRequestDTO request)
    {
        SellerProfile seller = sellerProfileRepository.findById(sellerId)
        		.orElseThrow(() -> new ResourceNotFoundException( "Seller not found" ) );

        // disabled seller cannot be modified
        if(seller.getSellerStatus() == SellerStatus.DEACTIVTED)
        {
            throw new RuntimeException("Seller already disabled");
        }

        User user = seller.getUser();

        // APPROVE
        if(request.getStatus() == SellerStatus.APPROVED)
        {
            seller.setSellerStatus(SellerStatus.APPROVED);

            seller.setVerificationDate( LocalDateTime.now());

            boolean alreadySeller = user.getRoles()
                            .stream()
                            .anyMatch(r -> r.getName() == RoleEnum.SELLER);

            if(!alreadySeller)
            {
                Role sellerRole = roleRepository.findByName(RoleEnum.SELLER)
                                .orElseThrow(() -> new RuntimeException("SELLER role not found"));

                user.getRoles().add(sellerRole);
            }
        }

        // REJECT
        else if(request.getStatus() == SellerStatus.REJECTED)
        {
            seller.setSellerStatus(SellerStatus.REJECTED);
            seller.setVerificationDate(null);
        }

        // DISABLE
        else if(request.getStatus() == SellerStatus.DEACTIVTED)
        {
            seller.setSellerStatus(SellerStatus.DEACTIVTED);

            // remove seller role
            user.getRoles().removeIf(role -> role.getName() == RoleEnum.SELLER);
        }

        userRepository.save(user);

        sellerProfileRepository.save(seller);
    }
}