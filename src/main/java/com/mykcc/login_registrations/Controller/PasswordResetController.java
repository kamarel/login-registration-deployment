package com.mykcc.login_registrations.Controller;


import com.mykcc.login_registrations.Entity.Users;
import com.mykcc.login_registrations.Repository.UserRepository;
import com.mykcc.login_registrations.Service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        try {
            Users user = userRepository.findByEmail(email);
            if (user != null) {
                String confirmationCode = passwordResetService.createConfirmationCodeForUser(user);
                passwordResetService.sendConfirmationCodeEmail(user, confirmationCode);
                return ResponseEntity.ok("Confirmation code sent to your email!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email address not found!");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request.");
        }
    }


    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestParam("code") String code, @RequestParam("password") String password) {
        if (passwordResetService.validateConfirmationCode(code)) {
            passwordResetService.resetPassword(code, password);
            return ResponseEntity.ok("Password reset successful.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired confirmation code.");
        }
    }
}
