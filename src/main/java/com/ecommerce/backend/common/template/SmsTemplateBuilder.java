package com.ecommerce.backend.common.template;

import org.springframework.stereotype.Component;

@Component
public class SmsTemplateBuilder {

	 public String buildPhoneVerificationOtp( String otp, String phone)
	    {
	        return """
	        		Thank you for registering with Ecommerce Platform.
	        		Your OTP is: %s to verify your phone number: %s.
	        		This OTP will expire in 5 minutes.
		            Do not share this OTP with anyone.
	        		If you did not create an account with us, please ignore this message.
	                """.formatted(otp, phone);          
	    }
	 
	 public String buildAccountReactivationOtp( String otp, String phone)
	    {
	        return """
	        	    Account Reactivation Request
		            Your OTP is: %s
		            Use this OTP to reactivate your account associated with phone number %s.
		            This OTP will expire in 5 minutes.
		            Do not share this OTP with anyone.
		            If you did not request account reactivation, please ignore this message.
	                """.formatted(otp, phone);	        
	    }

	 public String buildPhoneUpdatationOtp(String otp, String phone) 
	 {
		 return """
	        	    Phone Number Update Request
		            Your OTP is: %s
		            Use this OTP to verify and update your phone number to %s.
		            This OTP will expire in 5 minutes.
		            Do not share this OTP with anyone.
		            If you did not request this phone number update, please ignore this message.
	                """.formatted(otp, phone);	 
	 }
}

