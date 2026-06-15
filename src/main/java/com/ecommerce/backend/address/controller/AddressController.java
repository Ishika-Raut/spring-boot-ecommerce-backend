package com.ecommerce.backend.address.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.address.dto.AddressRequestDTO;
import com.ecommerce.backend.address.dto.AddressResponseDTO;
import com.ecommerce.backend.address.service.AddressService;
import com.ecommerce.backend.identity.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController 
{
    private final AddressService addressService;

    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping
    public ResponseEntity<AddressResponseDTO> addAddress(@AuthenticationPrincipal User user, @Valid @RequestBody AddressRequestDTO addressRequestDTO)
    {
        return ResponseEntity.ok(addressService.addAddress(user, addressRequestDTO));
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAddresses(@AuthenticationPrincipal User user)
    {
        return ResponseEntity.ok(addressService.getUserAddresses(user));
    }
    
    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddressById(@AuthenticationPrincipal User user, @PathVariable("id") Long addressId)
    {
        return ResponseEntity.ok(addressService.getAddressById(user, addressId));
    }
    
    @PreAuthorize("hasAnyRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(@AuthenticationPrincipal User user,  @PathVariable("id") Long addressId, AddressRequestDTO addressRequestDTO)
    {
        return ResponseEntity.ok(addressService.updateAddress(user, addressId, addressRequestDTO));
    }

    @PreAuthorize("hasAnyRole('USER')")   
    @PutMapping("/{id}/default")
    public ResponseEntity<String> setDefault(@AuthenticationPrincipal User user, @PathVariable Long id)
    {
        addressService.setDefaultAddress(user, id);
        return ResponseEntity.ok("Default address updated");
    }

    @PreAuthorize("hasAnyRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@AuthenticationPrincipal User user, @PathVariable Long id)
    {
        addressService.deleteAddress(user, id);
        return ResponseEntity.ok("Address deleted");
    }
}
