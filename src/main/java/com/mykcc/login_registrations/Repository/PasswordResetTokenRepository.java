package com.mykcc.login_registrations.Repository;



import com.mykcc.login_registrations.Entity.PasswordResetToken;
import com.mykcc.login_registrations.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByUser(Users user); // This finds by user
    PasswordResetToken findByToken(String token);
}