package com.mykcc.login_registrations.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void send(SimpleMailMessage email) {
        mailSender.send(email);
    }

    public void sendConfirmationCode(String email, String confirmationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Confirmation Code");
            message.setText("Your confirmation code is: " + confirmationCode);
            mailSender.send(message);
        } catch (MailException e) {
            // Log the exception
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}
