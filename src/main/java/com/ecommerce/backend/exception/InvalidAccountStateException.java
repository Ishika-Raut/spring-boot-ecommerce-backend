package com.ecommerce.backend.exception;

public class InvalidAccountStateException extends RuntimeException {
	public InvalidAccountStateException(String message)
    {
        super(message);
    }
}
