package com.ecommerce.backend.identity.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.enums.RoleEnum;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.enums.VerificationStatus;
import com.ecommerce.backend.enums.VerificationType;
import com.ecommerce.backend.exception.AccountNotVerifiedException;
import com.ecommerce.backend.exception.InvalidAccountStateException;
import com.ecommerce.backend.exception.InvalidOtpException;
import com.ecommerce.backend.exception.InvalidVerificationTokenException;
import com.ecommerce.backend.exception.OtpExpiredException;
import com.ecommerce.backend.exception.PasswordMismatchException;
import com.ecommerce.backend.exception.ResourceAlreadyExistsException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.exception.VerificationTokenAlreadyUsedException;
import com.ecommerce.backend.exception.VerificationTokenExpiredException;
import com.ecommerce.backend.identity.dto.AccountReactivationRequestDTO;
import com.ecommerce.backend.identity.dto.AuthenticationResponseDTO;
import com.ecommerce.backend.identity.dto.ForgotPasswordRequestDTO;
import com.ecommerce.backend.identity.dto.LoginRequestDTO;
import com.ecommerce.backend.identity.dto.RegisterRequestDTO;
import com.ecommerce.backend.identity.dto.RegisterResponseDTO;
import com.ecommerce.backend.identity.dto.ResetPasswordRequestDTO;
import com.ecommerce.backend.identity.dto.VerifyPhoneRequestDTO;
import com.ecommerce.backend.identity.entity.Role;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.entity.UsersVerification;
import com.ecommerce.backend.identity.repository.RoleRepository;
import com.ecommerce.backend.identity.repository.UserRepository;
import com.ecommerce.backend.identity.repository.UsersVerificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final TokenService tokenService;
	private final RoleRepository roleRepository;
	private final UsersVerificationRepository usersVerificationRepository;
	private final VerificationService verificationService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	@Value("${security.jwt.refresh-expiration-time}")
	private Long refreshExpirationTime;

	@Transactional // applied on methods are modifying the db read only methods- GET does not
					// require it
	public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
		// CHECK EXISTING EMAIL
		// Repository returns Optional to safely handle "data may or may not exist"
		Optional<User> existingEmailUser = userRepository.findByEmail(registerRequestDTO.getEmail()); // Fetches COMPLETE User entity from db wrapped: Optional<User>

		if (existingEmailUser.isPresent()) {
			User existingUser = existingEmailUser.get(); // This extracts the actual User object from Optional.

			// ACTIVE ACCOUNT STATUS => throw ResourceAlreadyExistsException
			if (existingUser.getStatus() == UserStatus.ACTIVE) {
				throw new ResourceAlreadyExistsException("Email already exists");
			}

			// ACCOUNT DELETED
			if (existingUser.getStatus() == UserStatus.DEACTIVATE) {
				throw new InvalidAccountStateException("Your account is deactivated. Please reactivate your account.");
			}
			// EMAIL VERIFIED BUT PHONE NOT VERIFIED
			// IF (emailVerified is TRUE) AND (phoneVerified is NOT TRUE)
			if (Boolean.TRUE.equals(existingUser.getEmailVerified()) // Boolean.TRUE.equals(true) → true
					&& !Boolean.TRUE.equals(existingUser.getPhoneVerified())) // Boolean.TRUE.equals(false) → false -->
																				// !false → true
			// true && true → TRUE (condition passes)
			{
				verificationService.sendPhoneVerificationOtp(existingUser, registerRequestDTO.getPhone(), VerificationType.PHONE_VERIFICATION);

				return RegisterResponseDTO.builder().message("Phone verification OTP resent")
						.email(existingUser.getEmail()).emailVerified(true).status(existingUser.getStatus().name())
						.build();
			}

			// INACTIVE USER => RESEND VERIFICATION
			verificationService.sendEmailVerification(existingUser);

			return RegisterResponseDTO.builder().message("Verification email resent").email(existingUser.getEmail())
					.emailVerified(existingUser.getEmailVerified()).status(existingUser.getStatus().name()).build();
		}

		// CHECK PHONE
		if (userRepository.existsByPhone(registerRequestDTO.getPhone())) {
			throw new ResourceAlreadyExistsException("Phone already exists");
		}

		// FETCH USER ROLE
		Role userRole = roleRepository.findByName(RoleEnum.USER)
				.orElseThrow(() -> new ResourceNotFoundException("USER role not found"));

		Set<Role> roles = new HashSet<>();
		roles.add(userRole);

		// CREATE USER
		User user = User.builder().name(registerRequestDTO.getName()).email(registerRequestDTO.getEmail())
				 .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
				//.password(registerRequestDTO.getPassword())
				.phone(registerRequestDTO.getPhone()).roles(roles)
				.status(UserStatus.INACTIVE).emailVerified(false).phoneVerified(false).build();

		userRepository.save(user);

		// SEND VERIFICATION EMAIL
		verificationService.sendEmailVerification(user);

		return RegisterResponseDTO.builder().message("Registration successful. Please verify your email.")
				.email(user.getEmail()).emailVerified(false).status(user.getStatus().name()).build();
	}

	@Transactional
	public User verifyEmail(String token) {
		// etch top 1st record where code = token and order by cretaed lastest = desc
		// order
		UsersVerification verification = usersVerificationRepository.findTopByCodeOrderByCreatedAtDesc(token)
				.orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));

		if (verification.isExpired()) {
			verification.setVerificationStatus(VerificationStatus.EXPIRED);
			throw new VerificationTokenExpiredException("Verification token expired");
		}

		if (Boolean.TRUE.equals(verification.getVerificationStatus() == VerificationStatus.USED)) {
			throw new VerificationTokenAlreadyUsedException("Token already used");
		}

		User user = verification.getUser();

		// mark verification as used
		verification.setVerificationStatus(VerificationStatus.USED);
		verification.setVerifiedAt(LocalDateTime.now());

		// update user
		if (!Boolean.TRUE.equals(user.getEmailVerified())) {
			user.setEmailVerified(true);
		}

//	    // if both verified → ACTIVE
//	    if (Boolean.TRUE.equals(user.getEmailVerified())
//	            && Boolean.TRUE.equals(user.getPhoneVerified())) {
//	        user.setStatus(UserStatus.ACTIVE);
//	    }

		userRepository.save(user);
		usersVerificationRepository.save(verification);

		return user;
	}

	@Transactional
	public void verifyPhoneOtp(VerifyPhoneRequestDTO verifyPhoneRequestDTO) 
	{
//		User existingUser = userRepository.findByPhone(verifyPhoneRequestDTO.getPhone())
//				.orElseThrow(() -> new ResourceNotFoundException("Entered phone number does not exist!"));

		// find top 1 record where target(phone number of user) = ? AND Code(otp) = ?
		// AND VerificationType = PHONE_VERIFICATION
		// AND used = false menas OTP already used nahi hona chahiye then order by
		// createdAt in DESC order
		UsersVerification verification = usersVerificationRepository
				.findTopByTargetAndVerificationTypeAndVerificationStatusOrderByCreatedAtDesc(
						verifyPhoneRequestDTO.getPhone(), verifyPhoneRequestDTO.getVerificationType(), VerificationStatus.ACTIVE)
				.orElseThrow(() -> new InvalidOtpException("Invalid OTP")); // OTP already used this case is handled
																			// here

		// ATTEMPTS LOGIC FOR WRONG OTP
		if (!verification.getCode().equals(verifyPhoneRequestDTO.getOtp())) {

			verification.incrementAttempts(); // Wrong OTP - increment attempts

			if (verification.getAttempts() >= 5) {
				verification.setVerificationStatus(VerificationStatus.LOCKED); // Wrong OTP - increment attempts
			}

			usersVerificationRepository.save(verification);

			throw new InvalidOtpException("Invalid OTP");
		}

		// EXPIRY CHECK
		if (verification.isExpired()) {
			verification.setVerificationStatus(VerificationStatus.EXPIRED);
			usersVerificationRepository.save(verification);
			throw new OtpExpiredException("OTP expired");
		}

		// mark OTP as used
		verification.setVerificationStatus(VerificationStatus.USED);
		verification.setVerifiedAt(LocalDateTime.now());
		
		User existingUser = verification.getUser();

		switch (verifyPhoneRequestDTO.getVerificationType()) 
		{
			case PHONE_VERIFICATION: 
			{
				// update user
				existingUser.setPhoneVerified(true);
	
				// both verified --> ACTIVE user
				if (Boolean.TRUE.equals(existingUser.getEmailVerified())
						&& Boolean.TRUE.equals(existingUser.getPhoneVerified())) {
					existingUser.setStatus(UserStatus.ACTIVE);
				}
				break;
			}
			case ACCOUNT_REACTIVATION: 
			{
				//update user status to ACTIVE
				existingUser.setStatus(UserStatus.ACTIVE);
				break;
			}

			case PHONE_CHANGE: 
			{
		        existingUser.setPhone(verification.getTarget());
		        break;
		    }
			
			default: throw new IllegalArgumentException("Unsupported verification type");
		}
		
		userRepository.save(existingUser);
		usersVerificationRepository.save(verification);
	}

	public void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
		User user = userRepository.findByEmail(forgotPasswordRequestDTO.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		verificationService.sendPasswordResetEmail(user);
	}

	@Transactional
	public void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
		UsersVerification verification = usersVerificationRepository
				.findTopByCodeAndVerificationTypeAndVerificationStatusOrderByCreatedAtDesc(
						resetPasswordRequestDTO.getToken(), VerificationType.PASSWORD_RESET, VerificationStatus.ACTIVE)
				.orElseThrow(() -> new InvalidVerificationTokenException("Invalid reset token"));

		// expiry check
		if (verification.isExpired()) {
			verification.setVerificationStatus(VerificationStatus.EXPIRED);
			usersVerificationRepository.save(verification);
			throw new VerificationTokenExpiredException("Reset token expired");
		}

		if (!resetPasswordRequestDTO.getNewPassword().equals(resetPasswordRequestDTO.getConfirmNewPassword())) {
			throw new PasswordMismatchException("Passwords do not match");
		}

		User user = verification.getUser();

		// update password
		// user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		user.setPassword(resetPasswordRequestDTO.getNewPassword());

		// mark token as USED
		verification.setVerificationStatus(VerificationStatus.USED);
		verification.setVerifiedAt(LocalDateTime.now());

		userRepository.save(user);
		usersVerificationRepository.save(verification);
	}

	@Transactional
	public AuthenticationResponseDTO login(LoginRequestDTO loginRequestDTO) 
	{
		// Yahan UsernamePasswordAuthenticationToken ka matlab hai: "User login karna chahta hai. Ye uska email aur password hai. Please verify karo."
		// Is stage par user authenticated nahi hai. Is object ke andar hota hai:
//		principal   = email
//		credentials = password
//		authenticated = false
		//Verify email/password
		
		//Phir Spring internally ye flow chalata hai: 
		//AuthenticationManager --> DaoAuthenticationProvider --> UserDetailsService.loadUserByUsername() --> PasswordEncoder.matches() --> sucss/fail
		//verify password
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));

		//Load authenticated user
	    User user = userRepository.findByEmail(loginRequestDTO.getEmail())
	            .orElseThrow(() -> new BadCredentialsException("User not found"));
	
	    if (!Boolean.TRUE.equals(user.getEmailVerified())) {
	        throw new AccountNotVerifiedException("Please verify your email first");
	    }
	
	    if (!Boolean.TRUE.equals(user.getPhoneVerified())) {
	        throw new AccountNotVerifiedException("Please verify your phone first");
	    }
	
	    if (user.getStatus() == UserStatus.DEACTIVATE) {
	        throw new InvalidAccountStateException("Your account is deactivated. Please reactivate your account.");
	    }
	
	    tokenService.revokedAllRefreshTokens(user);
	
	    user.setLastLoginAt(LocalDateTime.now());
	    userRepository.save(user);
	    
	    List<String> roles = user.getRoles()
	            .stream()
	            .map(r -> r.getName().name())
	            .toList();
	
	    String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getEmail(), roles);
	
	    String refreshToken = jwtService.generateRefreshToken(user.getEmail());
	
	    tokenService.saveRefreshToken(
	            user,
	            refreshToken,
	            refreshExpirationTime
	    );
	
	    String role = user.getRoles()
	            .stream()
	            .findFirst()
	            .map(r -> r.getName().name())
	            .orElse("USER");
	
	    return AuthenticationResponseDTO.builder()
	            .accessToken(accessToken)
	            .refreshToken(refreshToken)
	            .tokenType("Bearer")
	            .name(user.getName())
	            .email(user.getEmail())
	            .role(role)
	            .build();
	}
	
	public void accountReactivation(AccountReactivationRequestDTO accountReactivationRequestDTO) 
	{
		User user = userRepository.findByPhone(accountReactivationRequestDTO.getPhone())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (user.getStatus() != UserStatus.DEACTIVATE) {
			throw new InvalidAccountStateException("Account is not deactivated");
		}
		verificationService.sendPhoneVerificationOtp(user, accountReactivationRequestDTO.getPhone(), VerificationType.ACCOUNT_REACTIVATION);

	}
	
	/*
	 * POST /auth/login
        |
        V
AuthenticationManager.authenticate()
        |
        V
DaoAuthenticationProvider
        |
        V
UserDetailsService
        |
        V
Load User From DB
        |
        V
BCryptPasswordEncoder.matches()
        |
        +---- Wrong Password --> 401
        |
        V
Authenticated
        |
        V
Load User Again
        |
        V
Check:
- Email Verified?
- Phone Verified?
- Account Active?
        |
        +---- Fail --> 403
        |
        V
Revoke Old Refresh Tokens
        |
        V
Update Last Login
        |
        V
Generate Access Token
        |
        V
Generate Refresh Token
        |
        V
Store Refresh Token
        |
        V
Return Tokens
	 * */

}