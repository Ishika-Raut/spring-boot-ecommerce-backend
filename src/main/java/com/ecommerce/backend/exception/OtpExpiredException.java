package com.ecommerce.backend.exception;

public class OtpExpiredException extends RuntimeException
{
	  public OtpExpiredException(String message)
	  {
	      super(message);
	  }
}
