package com.mykcc.login_registrations.Service;


import com.mykcc.login_registrations.Entity.PasswordResetToken;
import com.mykcc.login_registrations.Entity.Users;
import com.mykcc.login_registrations.Repository.PasswordResetTokenRepository;
import com.mykcc.login_registrations.Repository.UserRepository;
import com.mykcc.login_registrations.Security.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public String createConfirmationCodeForUser(Users user) {
        // Check if a token already exists for the user
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user);
        if (existingToken != null) {
            // Optionally remove the existing token
            passwordResetTokenRepository.delete(existingToken);
        }

        String code = TokenUtil.generateRandomSixDigitCode(); // Generate a 6-digit random code
        Date expiryDate = calculateExpiryDate(15); // Example expiry time of 15 minutes
        PasswordResetToken resetToken = new PasswordResetToken(null, code, user, expiryDate); // Store the 6-digit code as the token
        passwordResetTokenRepository.save(resetToken);
        return code; // Return the generated code
    }


    public void sendConfirmationCodeEmail(Users user, String code) {
        String message = "Your password reset confirmation code is: " + code;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Password Reset Confirmation Code");
        mailMessage.setText(message);

        try {
            emailService.send(mailMessage);
        } catch (MailException e) {
            e.printStackTrace(); // Log exception
        }
    }

    public boolean validateConfirmationCode(String code) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(code);
        return passToken != null && !isTokenExpired(passToken);
    }

    public void resetPassword(String code, String newPassword) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(code);
        if (passToken != null) {
            Users user = passToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            passwordResetTokenRepository.delete(passToken); // This ensures the token is deleted after use
        }
    }


    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        return passToken.getExpiryDate().before(new Date());
    }
}