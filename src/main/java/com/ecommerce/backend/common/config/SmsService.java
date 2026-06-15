package com.ecommerce.backend.common.config;


import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService 
{
    private final TwilioConfig twilioConfig;

    public void sendSms( String toPhoneNumber, String messageBody ) 
    {

        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(twilioConfig.getPhoneNumber()),
                messageBody
        ).create();

        System.out.println("SMS sent successfully. SID: " + message.getSid());
    }
}