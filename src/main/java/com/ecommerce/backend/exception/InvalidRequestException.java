package com.ecommerce.backend.exception;

//Custom Exception Class → “What is the Problem?” - It only defines the error TYPE 

public class InvalidRequestException extends RuntimeException
{
    public InvalidRequestException(String message)
    {
        super(message);
    }
}