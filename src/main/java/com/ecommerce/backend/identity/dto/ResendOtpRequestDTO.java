package com.ecommerce.backend.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResendOtpRequestDTO 
{
	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
	private String phone;
}
