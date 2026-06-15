package com.ecommerce.backend.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponseDTO {

    private String message;

    private String email;

    private Boolean emailVerified;

    private String status;
}