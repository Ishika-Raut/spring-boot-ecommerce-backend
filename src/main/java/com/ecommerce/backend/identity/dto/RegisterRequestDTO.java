package com.ecommerce.backend.identity.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

	//@NotNull - work on Any object (String, Enum, Integer, DTO, etc.) value must NOT be null
	@NotBlank(message = "Name is required")  //work only on string - must NOT be null, empty, or whitespace
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
	private String phone;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
	private String password;
}
