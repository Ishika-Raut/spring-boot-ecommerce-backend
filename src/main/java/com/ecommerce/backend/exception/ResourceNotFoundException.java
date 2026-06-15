package com.ecommerce.backend.exception;

//Custom Exception Class → “What is the Problem?” - It only defines the error TYPE 
public class ResourceNotFoundException extends RuntimeException
{
    public ResourceNotFoundException(String message)
    {
        super(message);
    }
}