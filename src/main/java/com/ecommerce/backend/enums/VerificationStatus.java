package com.ecommerce.backend.enums;

public enum VerificationStatus {
	 ACTIVE, //when otp or token is generated
	    USED, //when user verifies it 
	    INVALIDATED, //when generating new otp or token and old are unused by user
	    EXPIRED, //when otp or token is expired
	    LOCKED	//
}
