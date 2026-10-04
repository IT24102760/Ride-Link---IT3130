package com.ridelink.accountservice.controller;

import com.ridelink.accountservice.dto.AccountResponse;
import com.ridelink.accountservice.dto.LoginRequest;
import com.ridelink.accountservice.dto.LoginResponse;
import com.ridelink.accountservice.dto.RegisterRequest;
import com.ridelink.accountservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Public endpoints: no token needed
@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Authentication")
@SecurityRequirements   // no lock icon in Swagger: these are public
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Register a passenger or driver account")
    @ApiResponse(responseCode = "201", description = "Account created (ACTIVE)")
    @ApiResponse(responseCode = "400", description = "Invalid input, or role ADMIN")
    @ApiResponse(responseCode = "409", description = "Email is already registered")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(@Valid @RequestBody RegisterRequest request) {
        return accountService.register(request);
    }

    @Operation(summary = "Log in and get a token",
            description = "Copy accessToken and paste it into Authorize in any RideLink service.")
    @ApiResponse(responseCode = "200", description = "Logged in")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @ApiResponse(responseCode = "403", description = "Account is SUSPENDED or DEACTIVATED")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request);
    }
}
