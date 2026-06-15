package com.ecommerce.backend.profile.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.identity.dto.VerifyPhoneRequestDTO;
import com.ecommerce.backend.identity.service.AuthenticationService;
import com.ecommerce.backend.profile.dto.UpdatePasswordRequestDTO;
import com.ecommerce.backend.profile.dto.UserProfileResponseDTO;
import com.ecommerce.backend.profile.dto.UserUpdateEmailRequestDTO;
import com.ecommerce.backend.profile.dto.UserUpdatePhoneRequestDTO;
import com.ecommerce.backend.profile.dto.UserUpdateProfileRequestDTO;
import com.ecommerce.backend.profile.dto.UserUpdateProfileResponseDTO;
import com.ecommerce.backend.profile.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController
{
    private final ProfileService profileService;
    private final AuthenticationService authenticationService;

    //Request first goes into Servlet Filter Chain. SS filter runs Once Per Request Filter our jwtAuthnFilter --> doFilterInternal()
    // GET PROFILE
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")  // one user account = one profile  so data fetched from User table row 
    @GetMapping
    public ResponseEntity<UserProfileResponseDTO> getProfile( Authentication authentication) //Spring injects Authentication object
    {      
    	String email = authentication.getName(); // authentication.getName() --> principal --> "johnDoe1s@gmail.com"
        return ResponseEntity.ok( profileService.getProfile(email));
    }

     //UPDATE PROFILE NAME
    @PreAuthorize("hasRole('USER')")
    @PutMapping
    public ResponseEntity<UserUpdateProfileResponseDTO> updateProfile(
            @Valid @RequestBody UserUpdateProfileRequestDTO userUpdateProfileRequestDTO,
            Authentication authentication
    )
    {
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.updateProfile(email, userUpdateProfileRequestDTO));
    }
    
    //CHANGE PASSWORD
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(Authentication authentication, @Valid @RequestBody UpdatePasswordRequestDTO updatePasswordRequestDTO)
    {
    	String email = authentication.getName();
        profileService.updatePassword(email, updatePasswordRequestDTO);
        return ResponseEntity.ok("Password changed successfully");
    }
    
    //UPDATE EMAIL
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@Valid @RequestBody UserUpdateEmailRequestDTO userUpdateEmailRequestDTO, Authentication authentication)
    {
    	System.out.println("authentication" + authentication);
        String email = authentication.getName();
        System.out.println("email = " +  email);
        profileService.updateEmail(email, userUpdateEmailRequestDTO);
        return ResponseEntity.ok("Email update request initiated. Please verify your new email.");
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmailChange( @RequestParam String token) 
    {
        profileService.verifyEmailChange(token);
        return ResponseEntity.ok("Your Email verified and updated successfully");
    }

    
    //UPDATE PHONE
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update-phone")
    public ResponseEntity<String> updatePhone(@Valid @RequestBody UserUpdatePhoneRequestDTO userUpdatePhoneRequestDTO, Authentication authentication)
    {
        String email = authentication.getName();
        profileService.updatePhone(email, userUpdatePhoneRequestDTO);
        return ResponseEntity.ok("Phone update request initiated. Please verify OTP.");
    }
    
    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhoneOtp(@Valid @RequestBody VerifyPhoneRequestDTO verifyPhoneRequestDTO)
    {
    	authenticationService.verifyPhoneOtp(verifyPhoneRequestDTO);
        return ResponseEntity.ok( "Your Phone verified and updated successfully");
    }

    // DELETE ACCOUNT
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(Authentication authentication)
    {
        String email = authentication.getName();

        profileService.deactivateAccount(email);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(0)
                        .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .path("/auth/refresh")
                        .maxAge(0)
                        .build();
        
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Account deavtivated successfully");
    }
}