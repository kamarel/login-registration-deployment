package com.mykcc.login_registrations.Controller;

import com.mykcc.login_registrations.Entity.LoginRequest;
import com.mykcc.login_registrations.Entity.LoginResponse;
import com.mykcc.login_registrations.Entity.RegistrastionResponse;
import com.mykcc.login_registrations.Entity.Users;
import com.mykcc.login_registrations.Security.JwtTokenProvider;
import com.mykcc.login_registrations.Service.CustomUserDetailsService;
import com.mykcc.login_registrations.Service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


@Tag(
        name=" Login Registration Controller Controller",
        description = "LoginRegistration Controller exposes Rest Api for LoginRegistration service"
)
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")


public class UserController {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider tokenProvider;





    @Operation(
            summary = "Log in Endpoint",
            description = "This Endpoint allow the user to log in into the website"
    )

    @ApiResponse(
            responseCode = "201",
            description = "HTTP Status 201 OK"
    )


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Load the user from the database
            Users user = userDetailsService.findByEmail(loginRequest.getEmail());

            // If the user doesn't exist, return an error
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Invalid credentials"));
            }

            // Proceed with authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = tokenProvider.generateToken(authentication);

            // Create a LoginResponse with token and success flag
            LoginResponse response = new LoginResponse(false, "Email not verified. Please verify your email first.");
            response.setSuccess(true);
            response.setMessage("Login successful");
            response.setUserData(jwt); // You can rename userData to 'token' if preferred

            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Email not verified. Please verify your email first."));
        } catch (Exception e) {
            // Handle other potential exceptions (e.g., database errors)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, "An error occurred during login"));
        }
    }







    @DeleteMapping("/login/users/delete/{id}")
    public ResponseEntity<String>deleteUser(@PathVariable Long id) {
        userDetailsService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/login/users")
    public ResponseEntity<List<Users>>getAllUsers() {
        List<Users> users = userDetailsService.getAllUsers();
        return ResponseEntity.ok(users);

    }

    @Operation(summary = "Send confirmation code to user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Confirmation code sent successfully"),
            @ApiResponse(responseCode = "400", description = "User already registered or email not found")
    })
    @PostMapping("/send-confirmation")
    public ResponseEntity<?> sendConfirmationCode(@RequestParam("email") String email) {
        Users user = userDetailsService.findByEmail(email);
        if (user != null && !user.isRegistered()) {
            String confirmationCode = String.format("%06d", new Random().nextInt(999999));
            user.setConfirmationCode(confirmationCode);
            user.setConfirmationCodeExpiration(LocalDateTime.now().plusMinutes(10)); // Code expires in 10 minutes
            userDetailsService.saveUser(user);
            emailService.sendConfirmationCode(email, confirmationCode);
            return ResponseEntity.ok("Confirmation code sent.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already registered or email not found.");
        }
    }







    @Operation(
            summary = "Logout Endpoint",
            description = "This Endpoint allow the user to logout the website"
    )

    @ApiResponse(
            responseCode = "201",
            description = "HTTP Status 201 OK"
    )

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            System.out.println("Logging out user: " + authentication.getName());
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            SecurityContextHolder.clearContext();  // Clear the authentication manually
        } else {
            System.out.println("No authentication found for the logout request.");
        }
        return ResponseEntity.ok("Logout successful");
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid  @RequestBody Users user) {
        // Check if the user already exists by email
        if (userDetailsService.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RegistrastionResponse(false, "User already exists with this email."));
        }

        // Save the user with encoded password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users savedUser = userDetailsService.saveUser(user);

        // Generate and send the confirmation code
        String confirmationCode = String.format("%06d", new Random().nextInt(999999));
        savedUser.setConfirmationCode(confirmationCode);
        savedUser.setConfirmationCodeExpiration(LocalDateTime.now().plusMinutes(10)); // Code expires in 10 minutes
        userDetailsService.saveUser(savedUser); // Save the user with the confirmation code

        emailService.sendConfirmationCode(savedUser.getEmail(), confirmationCode);

        // Return a JSON response with `success`
        return ResponseEntity.ok(new RegistrastionResponse(true, "User registered successfully. Please check your email for the confirmation code."));
    }





    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyConfirmationCode(@RequestParam("email") String email, @RequestParam("code") String code) {
        System.out.println("Received email: " + email + " and code: " + code); // Log the request

        Users user = userDetailsService.findByEmail(email);
        if (user == null || user.isRegistered()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RegistrastionResponse(false, "Invalid request or user already registered."));
        }

        if (!user.getConfirmationCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RegistrastionResponse(false, "Invalid confirmation code."));
        }

        if (user.getConfirmationCodeExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RegistrastionResponse(false, "Confirmation code expired."));
        }

        user.setEmailVerified(true);
        user.setConfirmationCode("NEW_CODE");
        user.setConfirmationCodeExpiration(LocalDateTime.now().plusMinutes(10));
        user.setRegistered(true); // Mark the user as registered
        userDetailsService.saveUser(user);

        return ResponseEntity.ok(new RegistrastionResponse(true, "Email verified successfully! You can now log in."));
    }



    @PostMapping("/resend-code")
    public ResponseEntity<?> resendConfirmationCode(@RequestParam("email") String email) {
        // Find the user by email
        Users user = userDetailsService.findByEmail(email);

        // Check if the user exists and has not been registered yet
        if (user == null || user.isRegistered()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RegistrastionResponse(false, "Invalid request or user already registered."));
        }

        // Generate a new confirmation code
        String newConfirmationCode = String.format("%06d", new Random().nextInt(999999));
        user.setConfirmationCode(newConfirmationCode);
        user.setConfirmationCodeExpiration(LocalDateTime.now().plusMinutes(10)); // Code expires in 10 minutes

        // Save the user with the new confirmation code
        userDetailsService.saveUser(user);

        // Send the new confirmation code to the user's email
        emailService.sendConfirmationCode(user.getEmail(), newConfirmationCode);

        // Return a response indicating success
        return ResponseEntity.ok(new RegistrastionResponse(true, "A new confirmation code has been sent to your email."));
    }


}