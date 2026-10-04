package com.ridelink.accountservice.config;

import com.ridelink.accountservice.model.Account;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Creates the first ADMIN account at startup from ADMIN_EMAIL / ADMIN_PASSWORD (if set).
// ADMIN cannot be chosen at registration, so this is the only way to get an admin.
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email:}") String adminEmail,
                       @Value("${app.admin.password:}") String adminPassword) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;   // not configured: skip
        }
        String email = adminEmail.trim().toLowerCase();
        if (accountRepository.existsByEmail(email)) {
            return;   // already created on an earlier start
        }
        Account admin = new Account();
        admin.setFullName("RideLink Admin");
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());
        accountRepository.save(admin);
        log.info("Created admin account {}", email);
    }
}
