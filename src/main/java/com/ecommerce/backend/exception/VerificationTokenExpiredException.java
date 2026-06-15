package com.ecommerce.backend.exception;

public class VerificationTokenExpiredException extends RuntimeException
{
    public VerificationTokenExpiredException(String message)
    {
        super(message);
    }
}
