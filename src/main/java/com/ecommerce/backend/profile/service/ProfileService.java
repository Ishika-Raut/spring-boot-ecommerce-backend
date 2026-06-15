package com.ecommerce.backend.profile.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.enums.VerificationStatus;
import com.ecommerce.backend.enums.VerificationType;
import com.ecommerce.backend.exception.InvalidVerificationTokenException;
import com.ecommerce.backend.exception.PasswordMismatchException;
import com.ecommerce.backend.exception.ResourceAlreadyExistsException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.exception.VerificationTokenAlreadyUsedException;
import com.ecommerce.backend.exception.VerificationTokenExpiredException;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.entity.UsersVerification;
import com.ecommerce.backend.identity.repository.UserRepository;
import com.ecommerce.backend.identity.repository.UsersVerificationRepository;
import com.ecommerce.backend.identity.service.TokenService;
import com.ecommerce.backend.identity.service.VerificationService;
import com.ecommerce.backend.profile.dto.UpdatePasswordRequestDTO;
import com.ecommerce.backend.profile.dto.UserProfileResponseDTO;
import com.ecommerce.backend.profile.dto.UserUpdateEmailRequestDTO;
import com.ecommerce.backend.profile.dto.UserUpdatePhoneRequestDTO;
import com.ecommerce.backend.profile.dto.UserUpdateProfileRequestDTO;
import com.ecommerce.backend.profile.dto.UserUpdateProfileResponseDTO;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService
{
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final UsersVerificationRepository usersVerificationRepository;

    public UserProfileResponseDTO getProfile(String email)
    {
    	User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        //JWT se user lo - so that current logged user + role ko profile dikhegi
    	System.out.println("in profile srvice GET");
        return UserProfileResponseDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    public UserUpdateProfileResponseDTO updateProfile(  String email, UserUpdateProfileRequestDTO userUpdateProfileRequestDTO)
    {
    	//Authentication object only has identity not complete entity details hence we need to fetch it from db
    	User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(  "User not found"));
    	
        user.setName(userUpdateProfileRequestDTO.getName());

        userRepository.save(user);
        return UserUpdateProfileResponseDTO.builder()
        .name(user.getName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .build();
    }
    
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequestDTO updatePasswordRequestDTO)
    {
    	User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    	// current password check
        if(!passwordEncoder.matches(updatePasswordRequestDTO.getCurrentPassword(), user.getPassword()))
    	//if((!updatePasswordRequestDTO.getCurrentPassword().equals(user.getPassword())))
        {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // confirm password check
        if(!updatePasswordRequestDTO.getNewPassword().equals(updatePasswordRequestDTO.getConfirmPassword()))
        {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // same password check
        if(passwordEncoder.matches(updatePasswordRequestDTO.getNewPassword(), user.getPassword()))
        {
            throw new PasswordMismatchException("New password cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(updatePasswordRequestDTO.getNewPassword()));
        //user.setPassword(updatePasswordRequestDTO.getNewPassword());

        userRepository.save(user);

        // revoke all refresh tokens
        tokenService.revokedAllRefreshTokens(user);
    }
    
    @Transactional
    public void updateEmail(String email, @Valid UserUpdateEmailRequestDTO userUpdateEmailRequestDTO) 
    {
    	User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    	
    	System.out.println("newEmail" + userUpdateEmailRequestDTO.getNewEmail());
    	System.out.println("old email" + email);
    	
		// check did user entered same email
    	if(email.equals(userUpdateEmailRequestDTO.getNewEmail()))
    		throw new ResourceAlreadyExistsException("This email is already registered with your account!");
    	
    	// check any other user have same email - global uniqueness
    	if (userRepository.existsByEmail(userUpdateEmailRequestDTO.getNewEmail())) {
    	    throw new ResourceAlreadyExistsException(
    	        "This email is already registered!"
    	    );
    	}
    	System.out.println("dto password = " + userUpdateEmailRequestDTO.getPassword());
    	System.out.println("db password = " + user.getPassword());
    	if (!passwordEncoder.matches(userUpdateEmailRequestDTO.getPassword(), user.getPassword())) 
    	{
    		throw new PasswordMismatchException("Invalid password!");
    	}
    	
    	 verificationService.sendEmailUpdateVerification(user, userUpdateEmailRequestDTO.getNewEmail());
	}
    
    @Transactional
	public User verifyEmailChange(String token) {
		// etch top 1st record where code = token and order by cretaed lastest = desc
		// order
		UsersVerification verification = usersVerificationRepository.findTopByCodeOrderByCreatedAtDesc(token)
				.orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));

		 if (verification.isExpired()) {
		        verification.setVerificationStatus(VerificationStatus.EXPIRED);
		        throw new VerificationTokenExpiredException("Verification token expired");
		    }

		    if (verification.getVerificationStatus() == VerificationStatus.USED) {
		        throw new VerificationTokenAlreadyUsedException("Token already used");
		    }
		
		User user = verification.getUser();
		String newEmail = verification.getTarget();
	
		if (userRepository.existsByEmail(newEmail)) {
	        throw new ResourceAlreadyExistsException("Email already in use");
	    }

	    user.setEmail(newEmail);

		// mark verification as used
		verification.setVerificationStatus(VerificationStatus.USED);
		verification.setVerifiedAt(LocalDateTime.now());
		
		tokenService.revokedAllRefreshTokens(user);

		userRepository.save(user);
		usersVerificationRepository.save(verification);

		return user;
	}
    
    @Transactional
    public void updatePhone(String email, @Valid UserUpdatePhoneRequestDTO userUpdatePhoneRequestDTO) 
    {
    	User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    	
		//check did user entered same phone 
    	if(user.getPhone().equals(userUpdatePhoneRequestDTO.getNewPhone()))
    	{
    		throw new ResourceAlreadyExistsException("This phone is already registered with your account!");
    	}
    	
    	//check any other user have same phone number - global uniqueness
    	if(userRepository.existsByPhone(userUpdatePhoneRequestDTO.getNewPhone()))
    	{
    	    throw new ResourceAlreadyExistsException("This phone number is already registered!");
    	}
    	
    	System.out.println("dto password = " + userUpdatePhoneRequestDTO.getPassword());
    	System.out.println("db password = " + user.getPassword());
    	if (!passwordEncoder.matches(userUpdatePhoneRequestDTO.getPassword(), user.getPassword())) 
    	{
    		throw new PasswordMismatchException("Invalid password!");
    	}
    	
    	 verificationService.sendPhoneVerificationOtp(user, userUpdatePhoneRequestDTO.getNewPhone(), VerificationType.PHONE_CHANGE);
	}
    
    @Transactional
    public void deactivateAccount(String email)
    {
    	User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(  "User not found"));

        tokenService.revokedAllRefreshTokens(user);

        user.setStatus(UserStatus.DEACTIVATE);

        userRepository.save(user);
    }
}
