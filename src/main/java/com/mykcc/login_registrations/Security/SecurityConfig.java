package com.mykcc.login_registrations.Security;


import com.mykcc.login_registrations.Service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {





    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions will be created or used
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/verify-code").permitAll()
                        .requestMatchers("/api/resend-code").permitAll()
                        .requestMatchers("/api/password/**").permitAll()
                        .requestMatchers("/api/send-confirmation").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/logout").authenticated() // Require authentication for logout
                        .requestMatchers("/api/login/users").permitAll()
                        .requestMatchers("/api/login/users/delete/{id}").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();  // Clear authentication manually (optional)
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logout successful");
                            response.getWriter().flush();
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }



























    //private final CustomUserDetailsService userDetailsService;

   /* public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions will be created or used
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/verify-code").permitAll()
                        .requestMatchers("/api/password/**").permitAll()
                        .requestMatchers("/api/send-confirmation").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/logout").authenticated() // Require authentication for logout
                        .anyRequest().authenticated()
                )

                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();  // Clear authentication manually
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logout successful");
                            response.getWriter().flush();
                        })
                );

        return http.build();
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }*/
}