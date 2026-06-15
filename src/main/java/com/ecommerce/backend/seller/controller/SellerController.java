package com.ecommerce.backend.seller.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.enums.SellerStatus;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.seller.dto.SellerApplyRequestDTO;
import com.ecommerce.backend.seller.dto.SellerUpdateRequestDTO;
import com.ecommerce.backend.seller.service.SellerProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController 
{
    private final SellerProfileService sellerProfileService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/apply")
    public ResponseEntity<String> applyForSeller(@AuthenticationPrincipal User user, @Valid @RequestBody SellerApplyRequestDTO sellerApplyRequestDTO)
    {
        sellerProfileService.applyForSeller(user, sellerApplyRequestDTO);
        return ResponseEntity.ok("Seller application submitted");
    }
    
    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/status")
    public ResponseEntity<SellerStatus> getApplicationStatus( @AuthenticationPrincipal User user)
    {
        return ResponseEntity.ok(sellerProfileService.getApplicationStatus(user));
    }
    
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/me")
	  public ResponseEntity<?> updateSellerProfile(@AuthenticationPrincipal User user, @Valid @RequestBody SellerUpdateRequestDTO sellerUpdateRequestDTO)
	  {
    	 sellerProfileService.updateSellerProfile( user, sellerUpdateRequestDTO ) ;   
	     return ResponseEntity.ok( "Seller profile updated successfully" );
	 }
    
    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/deactivate")
    public ResponseEntity<String> deactivateSellerAccount( @AuthenticationPrincipal User user)
    {
        sellerProfileService.deactivateSellerAccount(user);
        return ResponseEntity.ok( "Seller account deactivated successfully");
    }
    
    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/reactivate")
    public ResponseEntity<String> reactivateSellerAccount( @AuthenticationPrincipal User user)
    {
        sellerProfileService.reactivateSellerAccount(user);
        return ResponseEntity.ok( "Seller account reactivated successfully");
    }
    
//    @PreAuthorize("hasAnyRole('USER')")
//    @PostMapping
//    public ResponseEntity<AddressResponseDTO> addAddress(@AuthenticationPrincipal User user, @Valid @RequestBody AddressRequestDTO request)
//    {
//        return ResponseEntity.ok(addressService.addAddress(user, request));
//    }
//
//    @PreAuthorize("hasAnyRole('USER')")
//    @GetMapping
//    public ResponseEntity<List<AddressResponseDTO>> getAddresses(@AuthenticationPrincipal User user)
//    {
//        return ResponseEntity.ok(addressService.getUserAddresses(user));
//    }
//    
//    @PreAuthorize("hasAnyRole('USER')")
//    @GetMapping("/{id}")
//    public ResponseEntity<AddressResponseDTO> getAddressById(@AuthenticationPrincipal User user, Long addressId)
//    {
//        return ResponseEntity.ok(addressService.getAddressById(user, addressId));
//    }
//
//    @PreAuthorize("hasAnyRole('USER')")   
//    @PutMapping("/{id}/default")
//    public ResponseEntity<String> setDefault(@AuthenticationPrincipal User user, @PathVariable Long id)
//    {
//        addressService.setDefaultAddress(user, id);
//        return ResponseEntity.ok("Default address updated");
//    }
//
//    @PreAuthorize("hasAnyRole('USER')")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> delete(@AuthenticationPrincipal User user, @PathVariable Long id)
//    {
//        addressService.deleteAddress(user, id);
//        return ResponseEntity.ok("Address deleted");
//    }
	
}
