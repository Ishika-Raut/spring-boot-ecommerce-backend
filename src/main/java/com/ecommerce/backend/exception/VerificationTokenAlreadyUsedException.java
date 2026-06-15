package com.ecommerce.backend.exception;

public class VerificationTokenAlreadyUsedException extends RuntimeException {
	public VerificationTokenAlreadyUsedException(String message)
    {
        super(message);
    }
}
