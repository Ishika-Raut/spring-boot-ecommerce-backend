package com.ecommerce.backend.identity.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecommerce.backend.common.config.EmailService;
import com.ecommerce.backend.common.config.SmsService;
import com.ecommerce.backend.common.template.EmailTemplateBuilder;
import com.ecommerce.backend.common.template.SmsTemplateBuilder;
import com.ecommerce.backend.common.util.TokenAndOtpGenerator;
import com.ecommerce.backend.enums.CodeType;
import com.ecommerce.backend.enums.VerificationChannel;
import com.ecommerce.backend.enums.VerificationStatus;
import com.ecommerce.backend.enums.VerificationType;
import com.ecommerce.backend.exception.AccountNotVerifiedException;
import com.ecommerce.backend.exception.OtpResendCooldownException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.identity.dto.EmailRequestDTO;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.entity.UsersVerification;
import com.ecommerce.backend.identity.repository.UserRepository;
import com.ecommerce.backend.identity.repository.UsersVerificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final TokenService tokenService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final UsersVerificationRepository usersVerificationRepository;
    private final EmailTemplateBuilder templateBuilder;
    private final SmsTemplateBuilder smsTemplateBuilder;
    private final UserRepository userRepository;

    public void sendEmailVerification(User user)
    {
        UsersVerification verification = tokenService.generateToken( user, user.getEmail(), VerificationType.EMAIL_VERIFICATION, 24, ChronoUnit.HOURS);

        String verificationLink = "http://localhost:8080/auth/verify-email?token=" + verification.getCode();
        
        String body = templateBuilder.buildVerificationEmail(user.getName(), verificationLink);

        emailService.sendEmail(
                EmailRequestDTO.builder()
                        .to(user.getEmail())
                        .subject("Verify Email")
                        .body(body)
                        .isHtml(true)
                        .build()
        );
    }
    
    @Transactional
    public void sendPhoneVerificationOtp(User user, String phone, VerificationType verificationType)
    {
        // CHECK RESEND COOLDOWN
    	//method will: find top 1 record where user.userId = ? AND VerificationType = PHONE_VERIFICATION then order by createdAt in DESC order 
    	// latestOtp wrapped in optional
    	System.out.println("hiii-1");	
        Optional<UsersVerification> latestOtp = usersVerificationRepository.findTopByUserUserIdAndVerificationTypeOrderByCreatedAtDesc(
                                user.getUserId(),
                                verificationType
                        );
        System.out.println("latestOtp = " + latestOtp);
        if(latestOtp.isPresent())
        {
        	System.out.println("hiii-3");
            UsersVerification existingOtp = latestOtp.get(); //extract actual UsersVerification type object from the wrapper
            System.out.println("hiii-4");
            // ALLOW RESEND ONLY AFTER 30 SECONDS
            if(existingOtp.getCreatedAt().plusSeconds(30).isAfter(LocalDateTime.now()))
            {
            	System.out.println("hiii-5");
            	throw new OtpResendCooldownException("Please wait 30 seconds before requesting another OTP" );
            }
        }

        System.out.println("hiii-6");
        // INVALIDATE OLD OTPs
        usersVerificationRepository.invalidateOldTokens(user.getUserId(), verificationType);

        System.out.println("hiii-7");
        // GENERATE NEW OTP
        String otp = TokenAndOtpGenerator.generateOtp();

        System.out.println("otp = " + otp);
        UsersVerification verification = UsersVerification.builder()
                        .user(user)
                        .verificationType(verificationType )
                        .channel(VerificationChannel.SMS)
                        .target(phone)
                        .code(otp)
                        .codeType(CodeType.OTP)
                        .expiryTime(LocalDateTime.now().plusMinutes(5) ) //set expiry time for otp
                        .verificationStatus(VerificationStatus.ACTIVE)
                        .attempts(0)
                        .createdAt(LocalDateTime.now())
                        .build();

        usersVerificationRepository.save(verification);
        System.out.println("hiii-10");
        String message;
        switch (verificationType) 
        {

        	case PHONE_VERIFICATION:{
        		System.out.println("hiii-11");
        		message = smsTemplateBuilder.buildPhoneVerificationOtp(otp, phone);
        		System.out.println("hiii-12");
        		break;
        	}
	                       
	        case ACCOUNT_REACTIVATION: {
	        	System.out.println("hiii-13");
	        	message = smsTemplateBuilder.buildAccountReactivationOtp(otp, phone);
	        	break;
	        }
	        
	        case PHONE_CHANGE: {
	        	message = smsTemplateBuilder.buildPhoneUpdatationOtp(otp, phone);
	        	System.out.println("message = " + message);
	        	break;
	        }
	                               
	        default: throw new IllegalArgumentException("Unsupported verification type");
	    }
        System.out.println("hiii-15");
        smsService.sendSms(phone, message);
    }
    
    //if user  is on Verify Phone screen and Enter OTP --> but OTP expires --> User clicks button: Resend OTP
    //for this scenario resend otp API exists
    @Transactional
    public void resendPhoneOtp(String phone)
    {
        User user = userRepository.findByPhone(phone)
                        .orElseThrow(() -> new ResourceNotFoundException( "User not found"));
                        
        if(!Boolean.TRUE.equals(user.getEmailVerified()))
        {
            throw new AccountNotVerifiedException("Please verify email first");
        }

        if(Boolean.TRUE.equals(user.getPhoneVerified()))
        {
            throw new AccountNotVerifiedException("Phone already verified");
        }

        // invalidate old unused ACTIVE OTPs
        List<UsersVerification> oldOtps = usersVerificationRepository.findByTargetAndVerificationTypeAndVerificationStatus(
                        phone,
                        VerificationType.PHONE_VERIFICATION,
                        VerificationStatus.ACTIVE
                );

        for (UsersVerification otp : oldOtps) {
            otp.setVerificationStatus(VerificationStatus.INVALIDATED);
        }

        usersVerificationRepository.saveAll(oldOtps);
        sendPhoneVerificationOtp(user, phone, VerificationType.PHONE_VERIFICATION);
    }
    
    public void sendPasswordResetEmail(User user)
    {
        UsersVerification verification = tokenService.generateToken(user, user.getEmail(), VerificationType.PASSWORD_RESET, 30, ChronoUnit.MINUTES);

        String resetLink = "http://localhost:3000/reset-password?token=" + verification.getCode();
        
        String body = templateBuilder.buildResetPasswordEmail(user.getName(), resetLink);
       
        emailService.sendEmail(
                EmailRequestDTO.builder()
                        .to(user.getEmail())
                        .subject("Reset Password")
                        .body(body)
                        .isHtml(true)
                        .build()
        );
    }
    
    public void sendEmailUpdateVerification(User user, String email)
    {
        UsersVerification verification = tokenService.generateToken(user, email, VerificationType.EMAIL_CHANGE, 30, ChronoUnit.MINUTES);

        System.out.println("token = " + verification.getCode());
        String verificationLink = "http://localhost:3000/verify-email-change?token=" + verification.getCode();
        System.out.println("verificationLink = " + verificationLink);
        String body = templateBuilder.buildEmailUpdateTemplate(user.getName(), verificationLink);
        System.out.println("send emaiil = " + email);
        System.out.println("body = " + body);
        emailService.sendEmail(
                EmailRequestDTO.builder()
                        .to(email)
                        .subject("Verify your new email")
                        .body(body)
                        .isHtml(true)
                        .build()
        );
    }
    
} 
