package com.ecommerce.backend.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerApplyRequestDTO {

    @NotBlank
    @Size(max = 200)
    private String businessName;

    @NotBlank
    @Size(max = 150)
    private String ownerName;

    @NotBlank
    @Pattern(
            regexp = "^[0-9A-Z]{15}$",
            message = "Invalid GSTIN"
    )
    private String gstin;

    @NotBlank
    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN"
    )
    private String pan;

    @NotBlank
    private String bankAccountNumber;

    @NotBlank
    @Pattern(
            regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "Invalid IFSC code"
    )
    private String ifscCode;
}