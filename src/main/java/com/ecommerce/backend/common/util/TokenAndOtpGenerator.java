package com.ecommerce.backend.common.util;

import java.util.Random;
import java.util.UUID;

public class TokenAndOtpGenerator 
{
    public static String generateOtp() 
    {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    // Generates unique verification token
    public static String generateRandomToken() 
    {
        return UUID.randomUUID().toString();
    }
}

