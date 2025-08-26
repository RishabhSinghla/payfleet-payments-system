package com.payfleet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration - Temporarily Disables Spring Security
 * <p>
 * We're using BCryptPasswordEncoder for password encryption
 * but disabling the web security features for now.
 * <p>
 * Later we'll implement JWT authentication here.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF for API testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // Allow all requests for now
                );

        return http.build();
    }
}
