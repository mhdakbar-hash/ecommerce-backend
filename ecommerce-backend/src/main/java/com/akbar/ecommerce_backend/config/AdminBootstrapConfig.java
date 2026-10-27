package com.akbar.ecommerce_backend.config;

import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminBootstrapConfig implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AdminBootstrapConfig.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${app.admin-bootstrap.first-name:}")
    private String firstName;

    @Value("${app.admin-bootstrap.last-name:}")
    private String lastName;

    @Value("${app.admin-bootstrap.email:}")
    private String email;

    @Value("${app.admin-bootstrap.password:}")
    private String password;

    @Value("${app.admin-bootstrap.phone-number:}")
    private String phoneNumber;



    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Admin bootstrap is disabled.");
            return;
        }

        validateConfiguration();

        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("An admin account already exists. Bootstrap skipped.");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalStateException(
                    "Admin bootstrap email already belongs to an existing user. "
                            + "Use a different email or configure the existing account manually."
            );
        }

        UserEntity admin = UserEntity.builder()
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber.trim())
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.saveAndFlush(admin);

        log.info("Initial admin account created successfully.");
    }

    private void validateConfiguration() {
        if (isBlank(firstName)
                || isBlank(lastName)
                || isBlank(email)
                || isBlank(password)
                || isBlank(phoneNumber)) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled, but one or more required "
                            + "admin configuration values are missing."
            );
        }

        if (firstName.trim().length() > 50
                || lastName.trim().length() > 50) {
            throw new IllegalStateException(
                    "Admin first name and last name must not exceed 50 characters."
            );
        }

        if (email.trim().length() > 254) {
            throw new IllegalStateException(
                    "Admin email must not exceed 254 characters."
            );
        }

        if (password.length() > 72) {
            throw new IllegalStateException(
                    "Admin password must not exceed 72 characters for BCrypt."
            );
        }

        if (!phoneNumber.trim().matches("\\d{10}")) {
            throw new IllegalStateException(
                    "Admin phone number must contain exactly 10 digits."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}