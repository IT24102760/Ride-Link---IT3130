package com.ridelink.accountservice.service;

import com.ridelink.accountservice.dto.*;
import com.ridelink.accountservice.exception.ApiException;
import com.ridelink.accountservice.model.Account;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Tests AccountService with a fake repository and token service - no database needed
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();  // real hashing
    private AccountService accountService;

    private static final String PASSWORD = "Passenger@123";

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, passwordEncoder, jwtService);
    }

    // A stored account with a real password hash
    private Account account(String id, Role role, AccountStatus status) {
        Account a = new Account();
        a.setId(id);
        a.setFullName("Nimal Perera");
        a.setEmail("nimal@ridelink.test");
        a.setPasswordHash(passwordEncoder.encode(PASSWORD));
        a.setRole(role);
        a.setStatus(status);
        return a;
    }

    // Makes the fake repository return whatever is saved (with an id)
    private void saveReturnsInput() {
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            if (a.getId() == null) a.setId("acc-1");
            return a;
        });
    }

    // ----- register -----

    @Test
    void register_passenger_isActiveWithHashedPassword() {
        when(accountRepository.existsByEmail("nimal@ridelink.test")).thenReturn(false);
        saveReturnsInput();

        AccountResponse result = accountService.register(new RegisterRequest(
                "Nimal Perera", " Nimal@RideLink.test ", PASSWORD, "+94771234567", Role.PASSENGER));

        assertEquals("acc-1", result.id());
        assertEquals("nimal@ridelink.test", result.email());     // stored in lower case
        assertEquals(AccountStatus.ACTIVE, result.status());
        ArgumentCaptor<Account> saved = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(saved.capture());
        assertNotEquals(PASSWORD, saved.getValue().getPasswordHash());               // never the plain password
        assertTrue(passwordEncoder.matches(PASSWORD, saved.getValue().getPasswordHash()));
    }

    @Test
    void register_duplicateEmail_isConflict() {
        when(accountRepository.existsByEmail("nimal@ridelink.test")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> accountService.register(new RegisterRequest(
                "Nimal Perera", "nimal@ridelink.test", PASSWORD, null, Role.PASSENGER)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_asAdmin_isBadRequest() {
        ApiException ex = assertThrows(ApiException.class, () -> accountService.register(new RegisterRequest(
                "Hacker", "hacker@ridelink.test", PASSWORD, null, Role.ADMIN)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(accountRepository, never()).save(any());
    }

    // ----- login -----

    @Test
    void login_correctPassword_returnsToken() {
        when(accountRepository.findByEmail("nimal@ridelink.test"))
                .thenReturn(Optional.of(account("acc-1", Role.PASSENGER, AccountStatus.ACTIVE)));
        when(jwtService.createToken(any())).thenReturn("signed-token");
        when(jwtService.getExpirySeconds()).thenReturn(7200L);

        LoginResponse result = accountService.login(new LoginRequest("nimal@ridelink.test", PASSWORD));

        assertEquals("signed-token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals("acc-1", result.accountId());
        assertEquals(Role.PASSENGER, result.role());
    }

    @Test
    void login_wrongPassword_isUnauthorized() {
        when(accountRepository.findByEmail("nimal@ridelink.test"))
                .thenReturn(Optional.of(account("acc-1", Role.PASSENGER, AccountStatus.ACTIVE)));

        ApiException ex = assertThrows(ApiException.class,
                () -> accountService.login(new LoginRequest("nimal@ridelink.test", "wrong-password")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_unknownEmail_isUnauthorized() {
        when(accountRepository.findByEmail("nobody@ridelink.test")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> accountService.login(new LoginRequest("nobody@ridelink.test", PASSWORD)));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void login_suspendedAccount_isForbidden() {
        when(accountRepository.findByEmail("nimal@ridelink.test"))
                .thenReturn(Optional.of(account("acc-1", Role.DRIVER, AccountStatus.SUSPENDED)));

        ApiException ex = assertThrows(ApiException.class,
                () -> accountService.login(new LoginRequest("nimal@ridelink.test", PASSWORD)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verifyNoInteractions(jwtService);
    }

    // ----- profile -----

    @Test
    void updateMyProfile_changesNameAndPhone() {
        when(accountRepository.findById("acc-1"))
                .thenReturn(Optional.of(account("acc-1", Role.PASSENGER, AccountStatus.ACTIVE)));
        saveReturnsInput();

        AccountResponse result = accountService.updateMyProfile("acc-1",
                new UpdateProfileRequest("Nimal P.", "+94770000000"));

        assertEquals("Nimal P.", result.fullName());
        assertEquals("+94770000000", result.phone());
    }

    @Test
    void getMyAccount_unknownId_isNotFound() {
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> accountService.getMyAccount("missing"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    // ----- admin status -----

    @Test
    void updateStatus_byAdmin_suspendsAccount() {
        when(accountRepository.findById("acc-2"))
                .thenReturn(Optional.of(account("acc-2", Role.DRIVER, AccountStatus.ACTIVE)));
        saveReturnsInput();

        AccountResponse result = accountService.updateStatus("acc-2", AccountStatus.SUSPENDED, "admin-1");

        assertEquals(AccountStatus.SUSPENDED, result.status());
    }

    @Test
    void updateStatus_ownAccount_isConflict() {
        ApiException ex = assertThrows(ApiException.class,
                () -> accountService.updateStatus("admin-1", AccountStatus.SUSPENDED, "admin-1"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(accountRepository, never()).save(any());
    }
}
