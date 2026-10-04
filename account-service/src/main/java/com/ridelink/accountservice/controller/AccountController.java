package com.ridelink.accountservice.controller;

import com.ridelink.accountservice.dto.AccountResponse;
import com.ridelink.accountservice.dto.UpdateProfileRequest;
import com.ridelink.accountservice.dto.UpdateStatusRequest;
import com.ridelink.accountservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Account endpoints. The caller's id always comes from their token (jwt.getSubject()).
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // ===================== 2. My account =====================

    @Tag(name = "2. My account")
    @Operation(summary = "View my profile")
    @GetMapping("/me")
    public AccountResponse getMyAccount(@AuthenticationPrincipal Jwt jwt) {
        return accountService.getMyAccount(jwt.getSubject());
    }

    @Tag(name = "2. My account")
    @Operation(summary = "Update my name and phone")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PutMapping("/me")
    public AccountResponse updateMyAccount(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody UpdateProfileRequest request) {
        return accountService.updateMyProfile(jwt.getSubject(), request);
    }

    // ===================== 3. Admin =====================

    @Tag(name = "3. Admin")
    @Operation(summary = "List all accounts")
    @ApiResponse(responseCode = "403", description = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @Tag(name = "3. Admin")
    @Operation(summary = "View one account")
    @ApiResponse(responseCode = "403", description = "Admin only")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable String accountId) {
        return accountService.getAccount(accountId);
    }

    @Tag(name = "3. Admin")
    @Operation(summary = "Change an account's status",
            description = "SUSPENDED or DEACTIVATED accounts can no longer log in (403).")
    @ApiResponse(responseCode = "403", description = "Admin only")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "409", description = "An admin cannot change their own status")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{accountId}/status")
    public AccountResponse updateStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable String accountId,
                                        @Valid @RequestBody UpdateStatusRequest request) {
        return accountService.updateStatus(accountId, request.status(), jwt.getSubject());
    }
}
