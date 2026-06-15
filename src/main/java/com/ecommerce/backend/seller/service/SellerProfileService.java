package com.ecommerce.backend.seller.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecommerce.backend.enums.RoleEnum;
import com.ecommerce.backend.enums.SellerStatus;
import com.ecommerce.backend.exception.InvalidAccountStateException;
import com.ecommerce.backend.exception.ResourceAlreadyExistsException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.identity.entity.Role;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.repository.RoleRepository;
import com.ecommerce.backend.identity.repository.UserRepository;
import com.ecommerce.backend.seller.dto.SellerApplyRequestDTO;
import com.ecommerce.backend.seller.dto.SellerUpdateRequestDTO;
import com.ecommerce.backend.seller.entity.SellerProfile;
import com.ecommerce.backend.seller.repository.SellerProfileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerProfileService 
{
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
	public void applyForSeller(User user, SellerApplyRequestDTO sellerApplyRequestDTO)
	{
	    Optional<SellerProfile> existing = sellerProfileRepository.findByUser(user);
	
	    if(existing.isPresent())
	    {
	        SellerProfile existSeller = existing.get();
	
	        // Already applied or active seller
	        if(existSeller.getSellerStatus() == SellerStatus.PENDING || existSeller.getSellerStatus() == SellerStatus.APPROVED)
	        {
	            throw new ResourceAlreadyExistsException("Seller application already exists or already seller");
	        }
	    }
	    
	    if(sellerProfileRepository.existsByGstin( sellerApplyRequestDTO.getGstin()))
        {
            throw new ResourceAlreadyExistsException("GSTIN already exists");
        }

        if(sellerProfileRepository.existsByPan(sellerApplyRequestDTO.getPan()))
        {
            throw new ResourceAlreadyExistsException("PAN already exists");
        }

	    SellerProfile seller = SellerProfile.builder()
	    		.user(user)
	            .businessName(sellerApplyRequestDTO.getBusinessName())
	            .ownerName(sellerApplyRequestDTO.getOwnerName())
	            .gstin(sellerApplyRequestDTO.getGstin())
	            .pan(sellerApplyRequestDTO.getPan())
	            .bankAccountNumber(sellerApplyRequestDTO.getBankAccountNumber())
	            .ifscCode(sellerApplyRequestDTO.getIfscCode())
	            .sellerStatus(SellerStatus.PENDING)
	            .build();
	
	    sellerProfileRepository.save(seller);
	}
	    
	    
    public SellerStatus getApplicationStatus(User user)
    {
        SellerProfile seller = sellerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("No seller application found"));

        return seller.getSellerStatus();
    }
    
    @Transactional
	public void updateSellerProfile(User user, SellerUpdateRequestDTO sellerUpdateRequestDTO)
	{
	    SellerProfile seller = sellerProfileRepository.findByUser(user)
	                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
	
	  // DISABLED seller cannot update
	    if(seller.getSellerStatus() == SellerStatus.DEACTIVTED)
	    {
	        throw new InvalidAccountStateException("Seller account disabled");
	    }
	
	    // detect sensitive changes
	    boolean criticalChanged = false;
	
	    // GST changed
	    if(sellerUpdateRequestDTO.getGstin() != null && !sellerUpdateRequestDTO.getGstin().equals(seller.getGstin()))
	    {
	        // duplicate GST check
	        if(sellerProfileRepository.existsByGstin(sellerUpdateRequestDTO.getGstin()))
	        {
	            throw new ResourceAlreadyExistsException("GST already exists");
	        }
	
	        seller.setGstin( sellerUpdateRequestDTO.getGstin());
	
	        criticalChanged = true;
	    }
	
	    // PAN changed
	    if(sellerUpdateRequestDTO.getPan() != null && !sellerUpdateRequestDTO.getPan() .equals(seller.getPan()))
	    {
	        // duplicate PAN check
	        if(sellerProfileRepository.existsByPan(sellerUpdateRequestDTO.getPan()))
	        {
	            throw new ResourceAlreadyExistsException( "PAN already exists");
	        }
	
	        seller.setPan(sellerUpdateRequestDTO.getPan());
	
	        criticalChanged = true;
	    }
	
	    // BANK changed
	    if(sellerUpdateRequestDTO.getBankAccountNumber() != null && !sellerUpdateRequestDTO.getBankAccountNumber().equals(seller.getBankAccountNumber()))
	    {
	    	seller.setBankAccountNumber(sellerUpdateRequestDTO.getBankAccountNumber());	
	        criticalChanged = true;
	    }
	
	    // IFSC changed
	    if(sellerUpdateRequestDTO.getIfscCode() != null && !sellerUpdateRequestDTO.getIfscCode().equals(seller.getIfscCode()))
	    {
	    	seller.setIfscCode(sellerUpdateRequestDTO.getIfscCode());
	        criticalChanged = true;
	    }
	
	    // normal fields
	    if (sellerUpdateRequestDTO.getBusinessName() != null)
	        seller.setBusinessName(sellerUpdateRequestDTO.getBusinessName());

	    if (sellerUpdateRequestDTO.getOwnerName() != null)
	        seller.setOwnerName(sellerUpdateRequestDTO.getOwnerName());
	
	   //1. REJECTED seller updates profile--> becomes PENDING again
	   //2. APPROVED seller changes critical details --> becomes PENDING for admin review
	
	    if(seller.getSellerStatus() == SellerStatus.REJECTED || criticalChanged)
	    {
	        seller.setSellerStatus(SellerStatus.PENDING);
	        seller.setVerificationDate(null);
	    }
	
	    sellerProfileRepository.save(seller);
	}

	    
    @Transactional
    public void deactivateSellerAccount(User user)
    {
        SellerProfile seller = sellerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        seller.setSellerStatus(SellerStatus.DEACTIVTED);

        sellerProfileRepository.save(seller);

        // remove SELLER role
        user.getRoles().removeIf(
                r -> r.getName().name().equals("SELLER")
        );

        userRepository.save(user);
    }
	    
    @Transactional
    public void reactivateSellerAccount(User user)
    {
    	SellerProfile seller = sellerProfileRepository.findByUser(user)
    	        .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

    	if (seller.getSellerStatus() != SellerStatus.DEACTIVTED)
    	{
    	    throw new InvalidAccountStateException("Seller is not disabled");
    	}

    	seller.setSellerStatus(SellerStatus.PENDING);
    	seller.setVerificationDate(null);

    	sellerProfileRepository.save(seller);

    	// restore SELLER role
    	Role sellerRole = roleRepository.findByName(RoleEnum.SELLER)
                .orElseThrow(() -> new ResourceNotFoundException("SELLER role not found"));

        if (!user.getRoles().contains(sellerRole))
        {
            user.getRoles().add(sellerRole);
        }

    	userRepository.save(user);
    }
	    
}