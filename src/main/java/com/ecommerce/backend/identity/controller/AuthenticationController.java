package com.ecommerce.backend.identity.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.enums.VerificationType;
import com.ecommerce.backend.identity.dto.AccountReactivationRequestDTO;
import com.ecommerce.backend.identity.dto.AuthenticationResponseDTO;
import com.ecommerce.backend.identity.dto.ForgotPasswordRequestDTO;
import com.ecommerce.backend.identity.dto.LoginRequestDTO;
import com.ecommerce.backend.identity.dto.RefreshTokenRequestDTO;
import com.ecommerce.backend.identity.dto.RegisterRequestDTO;
import com.ecommerce.backend.identity.dto.RegisterResponseDTO;
import com.ecommerce.backend.identity.dto.ResendOtpRequestDTO;
import com.ecommerce.backend.identity.dto.ResetPasswordRequestDTO;
import com.ecommerce.backend.identity.dto.VerifyPhoneRequestDTO;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.service.AuthenticationService;
import com.ecommerce.backend.identity.service.TokenService;
import com.ecommerce.backend.identity.service.VerificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthenticationController 
{
	
	//Because in modern Spring Boot: constructor injection is preferred over: field injection using @Autowired
	//That is why used: @RequiredArgsConstructor with final fields.
	private final AuthenticationService authenticationService;
    private final VerificationService verificationService;
    private final TokenService tokenService;
    
    //NEVER DO THIS WRONG. Because: final already means constructor injection. @Autowired becomes unnecessary.
    //@Autowired
    //private final UserRepository userRepository;

    //@Valid - triggers DTOs validations vs @Validated ?
    /*
	    SIGNUP FLOW
	    1. User submits details
	    2. Save user as INACTIVE + emailVerified=false + phoneVerified=false
	    3. DO NOT generate JWT here
	*/
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerUserDto)
    {
    	RegisterResponseDTO registeredUser = authenticationService.register(registerUserDto);
        return ResponseEntity.ok(registeredUser); //shortcut for below return statement

        //return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
	    EMAIL VERIFICATION FLOW
	    1. Generate verification token and send it with link to user
	    2. 
	    Frontend URL: /auth/verify-email?token=abc123
	*/
    //because user: email me link click karta hai. Browser automatically: GET request bhejta hai. Isliye: GET + query param used.
    //Why query param - Because: verification link URL me hi token carry karta hai
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail( @RequestParam String token ) 
    {
        User user = authenticationService.verifyEmail(token);
        verificationService.sendPhoneVerificationOtp(user, user.getPhone(), VerificationType.PHONE_VERIFICATION);
        return ResponseEntity.ok("Email verified successfully");
    }

    //POST because: user manually OTP enter karta hai
    //Why OTP URL me nahi bhejte?
    //because URLs:  /verify-phone?otp=123456
    //browser history me save hote hain, logs me aa sakte hain, proxy/server logs me visible hote hain, OTP sensitive hota hai.
    //So: POST + RequestBody
    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhoneOtp(@Valid @RequestBody VerifyPhoneRequestDTO verifyPhoneRequestDTO)
    {
        authenticationService.verifyPhoneOtp(verifyPhoneRequestDTO);
        return ResponseEntity.ok( "Phone verified successfully");
    }
    
    
    @PostMapping("/resend-phone-otp")
    public ResponseEntity<String> resendPhoneOtp(@Valid @RequestBody ResendOtpRequestDTO resendOtpRequestDTO) 
    {
        verificationService.resendPhoneOtp(resendOtpRequestDTO.getPhone());
        return ResponseEntity.ok("OTP resent successfully");
    }
    
    
    @PostMapping("/login")
	public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO)
	{
    	System.out.println("hello-1");
	    AuthenticationResponseDTO response = authenticationService.login(loginRequestDTO);
	    System.out.println("hello-2");
	
	    ResponseCookie accessCookie = ResponseCookie.from( "accessToken",response.getAccessToken())
										            .httpOnly(true) //JavaScript cannot access tokens. Protects against XSS attacks
										            .secure(true) //Sent over HTTPS
										            .path("/")
										            .maxAge(Duration.ofMinutes(15))
										            .sameSite("Strict") //Protects against CSRF attacks. Cookies only sent in same-site requests
										            .build();
	
	    ResponseCookie refreshCookie = ResponseCookie.from( "refreshToken", response.getRefreshToken())
										            .httpOnly(true)
										            .secure(true)
										            .path("/auth/refresh")
										            .maxAge(Duration.ofDays(7))
										            .sameSite("Strict")
										            .build();
	
	    return ResponseEntity.ok()
				            .header(HttpHeaders.SET_COOKIE,  accessCookie.toString())
				            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				            .body(response);
	}
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO)
    {
        tokenService.logout( refreshTokenRequestDTO.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO)
    {
        return ResponseEntity.ok(tokenService.refreshAccessToken(refreshTokenRequestDTO.getRefreshToken()));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO)
    {
        authenticationService.forgotPassword(forgotPasswordRequestDTO);
        return ResponseEntity.ok("Password reset email sent");
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO)
    {
        authenticationService.resetPassword(resetPasswordRequestDTO);
        return ResponseEntity.ok("Password reset successful");
    }
    
    @PostMapping("/reactivation/request")
    public ResponseEntity<String> accountReactivation(@Valid @RequestBody AccountReactivationRequestDTO accountReactivationRequestDTO)
    {
        authenticationService.accountReactivation(accountReactivationRequestDTO);
        return ResponseEntity.ok("OTP sent for account reactivation");
    }
    
    @PostMapping("/reactivation/verify")
    public ResponseEntity<String> accountReactivationVerify(@Valid @RequestBody VerifyPhoneRequestDTO verifyPhoneRequestDTO)
    {
        authenticationService.verifyPhoneOtp(verifyPhoneRequestDTO);
        return ResponseEntity.ok("Your account has been reactivated!");
    }
    
      
}
