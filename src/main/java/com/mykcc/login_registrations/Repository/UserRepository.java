package com.mykcc.login_registrations.Repository;


import com.mykcc.login_registrations.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {



    Users findByEmail(String email);
}
