package com.ecommerce.backend.common.config;



import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.identity.dto.EmailRequestDTO;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService 
{
    private final JavaMailSender mailSender;

    public void sendEmail(EmailRequestDTO request) 
    {
        try 
        {
            MimeMessage message = mailSender.createMimeMessage(); //Create email msg: A raw email object (like a blank email draft)
            MimeMessageHelper helper = new MimeMessageHelper(message, true); //helper class to easily fill empty measage object
            //normal email is plain text only
            //true - means enabling multipart - multipaert means supproting multiple parts - html, attachments, images, pdfs, etc

            //Set email details from EmailRequestDTO
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());

            mailSender.send(message); //send messgae object to smtp server

        } 
        catch (Exception e) 
        {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
