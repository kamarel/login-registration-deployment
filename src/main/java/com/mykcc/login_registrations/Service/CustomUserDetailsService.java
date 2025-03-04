package com.mykcc.login_registrations.Service;


import com.mykcc.login_registrations.Entity.Users;
import com.mykcc.login_registrations.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Users saveUser(Users user) {


        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Roles must be provided");
        }
        System.out.println("Encoded password at registration: " + passwordEncoder);
        return userRepository.save(user);



    }

    public Users findByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email);




        if (user == null || !user.isEmailVerified()) {
            throw new UsernameNotFoundException("User not found with email or email not verified: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEmailVerified(),
                true,
                true,
                true,
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Users user) {
        return user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }


    public void sendConfirmationCode(String email, String confirmationCode) {
        String message = "Your confirmation code is: " + confirmationCode;
        emailService.sendConfirmationCode(email,  message); // Assuming you have an email sender set up
    }



}