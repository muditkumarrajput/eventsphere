package com.eventsphere.eventsphere_backend.auth.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendOtp(
            String phoneNumber,
            String otp,
            String purpose) {

        /*
         * TODO:
         * Integrate an SMS provider such as Twilio,
         * MSG91, or another production SMS service.
         *
         * The OTP must never be returned from an API response.
         */

        System.out.println(
                "SMS OTP sent to "
                        + phoneNumber
                        + " for "
                        + purpose
        );
    }
}