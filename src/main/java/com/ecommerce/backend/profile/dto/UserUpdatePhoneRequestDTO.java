package com.ecommerce.backend.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdatePhoneRequestDTO 
{

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
	private String newPhone;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
	private String password;
}
