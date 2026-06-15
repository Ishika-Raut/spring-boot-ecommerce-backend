package com.ecommerce.backend.exception;

//Custom Exception Class → “What is the Problem?” - It only defines the error TYPE 
//If don't use this then for all the exceptions 500 will be send
public class ResourceAlreadyExistsException extends RuntimeException
{
    public ResourceAlreadyExistsException(String message)
    {
        super(message);
    }
}