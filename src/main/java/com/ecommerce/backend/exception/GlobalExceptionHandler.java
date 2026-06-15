package com.ecommerce.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler
{

    // VALIDATION EXCEPTION
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException exception)
    {
        String errorMessage = exception
                .getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        errorMessage
                );

        problemDetail.setProperty(
                "description",
                "Request validation failed"
        );

        return problemDetail;
    }
    
    // JWT TOKEN INVALID EXCEPTION
    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleJwt(
            JwtException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid JWT token");
    }
    
    //JWT TOKEN EXPIRED
    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpiredJwt(
            ExpiredJwtException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Access token expired");
    }


    // RESOURCE ALREADY EXISTS
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ProblemDetail handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Resource already exists"
        );

        return problemDetail;
    }


    // RESOURCE NOT FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(
            ResourceNotFoundException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Requested resource not found"
        );

        return problemDetail;
    }


    // PASSWORD MISMATCH
    @ExceptionHandler(PasswordMismatchException.class)
    public ProblemDetail handlePasswordMismatchException(
            PasswordMismatchException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Passwords do not match"
        );

        return problemDetail;
    }


    // INVALID LOGIN CREDENTIALS
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(
            BadCredentialsException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Invalid authentication credentials"
        );

        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(
    		AccessDeniedException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Invalid authentication rights"
        );

        return problemDetail;
    }

    // ACCOUNT STATUS
//    @ExceptionHandler(AccountStatusException.class)
//    public ProblemDetail handleAccountStatusException(
//            AccountStatusException exception)
//    {
//        ProblemDetail problemDetail =
//                ProblemDetail.forStatusAndDetail(
//                        HttpStatus.FORBIDDEN,
//                        exception.getMessage()
//                );
//
//        problemDetail.setProperty(
//                "description",
//                "Account access restricted"
//        );
//
//        return problemDetail;
//    }


    // ACCOUNT NOT VERIFIED
    @ExceptionHandler(AccountNotVerifiedException.class)
    public ProblemDetail handleAccountNotVerifiedException(
            AccountNotVerifiedException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Account verification required"
        );

        return problemDetail;
    }
    
    //ACCOUNT STATUS
    @ExceptionHandler(InvalidAccountStateException.class)
    public ProblemDetail handleInvalidAccountStateException(
    		InvalidAccountStateException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Account is in an invalid state for this operation"
        );

        return problemDetail;
    }
    


    // INVALID VERIFICATION TOKEN
    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ProblemDetail handleInvalidTokenException(
            InvalidVerificationTokenException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Invalid or malformed token"
        );

        return problemDetail;
    }


    //VERIFICATION TOKEN EXPIRED
    @ExceptionHandler(VerificationTokenExpiredException.class)
    public ProblemDetail handleTokenExpiredException(
            VerificationTokenExpiredException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Token has expired"
        );

        return problemDetail;
    }
    
 // VERIFICATION TOKEN ALREADY USED
    @ExceptionHandler(VerificationTokenAlreadyUsedException.class)
    public ProblemDetail handleTokenAlreadyUsedException(
    		VerificationTokenAlreadyUsedException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Token has already been used"
        );

        return problemDetail;
    }
    
    
    // IF FRONTEND SENDS INVALID ENUM VALUE SPRING THROWS: HttpMessageNotReadableException
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(
    		HttpMessageNotReadableException exception)
    {
    	 ProblemDetail problemDetail =
    	            ProblemDetail.forStatusAndDetail(
    	                    HttpStatus.BAD_REQUEST,
    	                    "Malformed request body"
    	            );

    	    problemDetail.setProperty(
    	            "description",
    	            "Invalid request format or enum value provided"
    	    );
    	    
    	    return problemDetail;
    }

    // INVALID OTP
    @ExceptionHandler(InvalidOtpException.class)
    public ProblemDetail handleInvalidOtpException(
            InvalidOtpException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Invalid one-time password"
        );

        return problemDetail;
    }


    // OTP EXPIRED
    @ExceptionHandler(OtpExpiredException.class)
    public ProblemDetail handleOtpExpiredException(
            OtpExpiredException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "One-time password has expired"
        );

        return problemDetail;
    }
    
 // OTP EXPIRED
    @ExceptionHandler(OtpResendCooldownException.class)
    public ProblemDetail handleOtpResendCooldownException(
    		OtpResendCooldownException exception)
    {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS,
                        exception.getMessage()
                );

        problemDetail.setProperty(
                "description",
                "Too many requests"
        );

        return problemDetail;
    }
}