package com.ecommerce.backend.identity.dto;

import com.ecommerce.backend.enums.VerificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyPhoneRequestDTO {

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
	private String phone;

	@NotBlank(message = "OTP is required")
	@Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit number")
	private String otp;
	
	@NotNull(message = "Verification type is required") //work on Any object (String, Enum, Integer, DTO, etc.) value must NOT be null
	private VerificationType verificationType; 
	//Spring automatically: "PHONE_VERIFICATION" String --> PHONE_VERIFICATION Enum
	//If Frontend sends invalid enum value then Spring will throw: HttpMessageNotReadableException, Hence, FE MUST send exact enum value
}