package com.ecommerce.backend.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.backend.admin.service.AdminSellerService;
import com.ecommerce.backend.seller.dto.SellerStatusUpdateRequestDTO;
import com.ecommerce.backend.seller.entity.SellerProfile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/sellers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    @GetMapping("/pending")
    public ResponseEntity<List<SellerProfile>>
    getPendingApplications()
    {
        return ResponseEntity.ok(adminSellerService.getPendingApplications());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<SellerProfile>>
    getApprovedSellers()
    {
        return ResponseEntity.ok(
                adminSellerService
                        .getApprovedSellers()
        );
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<SellerProfile>>
    getRejectedApplications()
    {
        return ResponseEntity.ok(
                adminSellerService
                        .getRejectedApplications()
        );
    }

    @PatchMapping("/{sellerId}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long sellerId,
            @RequestBody
            SellerStatusUpdateRequestDTO request
    )
    {
        adminSellerService.updateSellerStatus(
                sellerId,
                request
        );

        return ResponseEntity.ok(
                "Seller status updated successfully"
        );
    }
}