package com.dorm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    private static final String SALT = "DormSystem2026";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder();
    }

    public static class PasswordEncoder {
        public String encode(CharSequence rawPassword) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(SALT.getBytes(StandardCharsets.UTF_8));
                byte[] hash = md.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
                return "{SHA256}" + Base64.getEncoder().encodeToString(hash);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            if (encodedPassword == null) return false;
            return encode(rawPassword).equals(encodedPassword);
        }
    }
}
