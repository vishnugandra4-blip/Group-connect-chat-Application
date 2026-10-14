package com.groupconnect.controller;

import com.groupconnect.dto.auth.AuthResponse;
import com.groupconnect.dto.auth.LoginRequest;
import com.groupconnect.dto.auth.RegisterRequest;
import com.groupconnect.dto.auth.UserSummaryDto;
import com.groupconnect.security.UserPrincipal;
import com.groupconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserSummaryDto userSummary = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(userSummary);
    }
}
