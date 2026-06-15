package com.ecommerce.backend.seller.dto;

import com.ecommerce.backend.enums.SellerStatus;

import lombok.Data;

@Data
public class SellerStatusUpdateRequestDTO {

    private SellerStatus status; // APPROVED / REJECTED
}
