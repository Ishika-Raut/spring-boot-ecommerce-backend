package com.ecommerce.backend.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerUpdateRequestDTO {

    @NotBlank
    @Size(max = 200)
    private String businessName;

    @NotBlank
    @Size(max = 150)
    private String ownerName;

    @NotBlank
    private String gstin;

    @NotBlank
    private String pan;
    
    @NotBlank
    private String bankAccountNumber;

    @NotBlank
    private String ifscCode;
}
