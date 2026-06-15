package com.ecommerce.backend.exception;


public class OtpResendCooldownException
        extends RuntimeException {

    public OtpResendCooldownException(String message) {
        super(message);
    }
}
