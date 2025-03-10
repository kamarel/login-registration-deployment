package com.mykcc.login_registrations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class LoginRegistrationsApplication {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public static void main(String[] args) {
		System.setProperty("eureka.client.service-url.defaultZone", "https://protective-determination-production.up.railway.app/eureka/");
		SpringApplication.run(LoginRegistrationsApplication.class, args);
	}

}
