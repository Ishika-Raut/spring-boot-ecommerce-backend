package com.ecommerce.backend.common.template;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplateBuilder 
{
	public String buildVerificationEmail(String name, String verificationLink) 
	{
	    return """
	        <div>
                <h2> Verify Your Email </h2>
	    		<p>Hello %s,</p>
                <p> Thank you for registering with Ecommerce Platform. </p>
                <p> Please click the button below to verify your email address. </p>

                <div>
                    <p>%s</p>
                </div>

                <p> This verification link will expire in 24 hours. </p>
                <hr>
                <p> If you did not create this account, please ignore this email. </p>

	        </div>

	        """.formatted(name, verificationLink);
	}

    public String buildResetPasswordEmail(String name, String resetLink) 
    {
        return """
                <div>
			        <h2> Reset your password </h2>
					<p>Hello %s,</p>
			        <p> Please click the button below and reset your password. </p>
			
			        <div>
			            <p>%s</p>
			        </div>
			
			        <p> This reset link will expire in 30 minutes. </p>
			        <hr>
			        <p> If you did not requested for reset password, please ignore this email. </p>
			
			    </div>
                """.formatted(name, resetLink);	
    }

	public String buildEmailUpdateTemplate(String name, String verificationLink) 
	{
		return """
                <div>
			        <h2> Verify your new email </h2>
					<p>Hello %s,</p>
			        <p> Please click the button below and verify your new email. </p>
			        
			        <div>
			            <p>%s</p>
			        </div>
			
			        <p> This verification link will expire in 30 minutes. </p>
			        <hr>
			        <p> If you did not requested for changing email, please ignore this email. </p>
			
			    </div>
                """.formatted(name, verificationLink);	
	}
}