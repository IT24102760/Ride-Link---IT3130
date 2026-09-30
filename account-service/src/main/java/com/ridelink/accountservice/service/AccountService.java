package com.ridelink.accountservice.service;

import com.ridelink.accountservice.dto.*;
import com.ridelink.accountservice.exception.ApiException;
import com.ridelink.accountservice.model.Account;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

// Business rules for accounts: register, login, profile and status
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // New passenger or driver -> ACTIVE account (password stored as a BCrypt hash)
    public AccountResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can only register as PASSENGER or DRIVER");
        }
        String email = normalise(request.email());
        if (accountRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        Account account = new Account();
        account.setFullName(request.fullName().trim());
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setPhone(request.phone());
        account.setRole(request.role());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());
        return AccountResponse.from(accountRepository.save(account));
    }

    // Correct email + password + ACTIVE account -> signed token
    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(normalise(request.email()))
                .filter(a -> passwordEncoder.matches(request.password(), a.getPasswordHash()))
                // same message for unknown email and wrong password, so emails can't be guessed
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is " + account.getStatus());
        }
        String token = jwtService.createToken(account);
        return new LoginResponse(token, "Bearer", jwtService.getExpirySeconds(),
                account.getId(), account.getRole());
    }

    // The caller's own profile
    public AccountResponse getMyAccount(String accountId) {
        return AccountResponse.from(findAccount(accountId));
    }

    // The caller updates their own name and phone
    public AccountResponse updateMyProfile(String accountId, UpdateProfileRequest request) {
        Account account = findAccount(accountId);
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone());
        account.setUpdatedAt(Instant.now());
        return AccountResponse.from(accountRepository.save(account));
    }

    // Admin: every account
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(AccountResponse::from).toList();
    }

    // Admin: one account
    public AccountResponse getAccount(String accountId) {
        return AccountResponse.from(findAccount(accountId));
    }

    // Admin: suspend, deactivate or reactivate an account (a non-ACTIVE account cannot log in)
    public AccountResponse updateStatus(String accountId, AccountStatus status, String adminId) {
        if (accountId.equals(adminId)) {
            throw new ApiException(HttpStatus.CONFLICT, "You cannot change your own status");
        }
        Account account = findAccount(accountId);
        account.setStatus(status);
        account.setUpdatedAt(Instant.now());
        return AccountResponse.from(accountRepository.save(account));
    }

    // ---------- helpers ----------

    private Account findAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    // Emails are compared in lower case without spaces
    private String normalise(String email) {
        return email.trim().toLowerCase();
    }
}
