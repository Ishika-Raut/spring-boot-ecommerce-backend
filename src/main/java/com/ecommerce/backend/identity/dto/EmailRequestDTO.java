package com.ecommerce.backend.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailRequestDTO {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid recipient email")
    private String to;

    @NotBlank(message = "Email subject is required")
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;

    @NotBlank(message = "Email body is required")
    @Size(max = 10000, message = "Email body is too long")
    private String body;

    private boolean isHtml;
}