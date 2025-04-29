package org.example.popitkan5.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.popitkan5.dto.AuthRequest;
import org.example.popitkan5.dto.AuthResponse;
import org.example.popitkan5.dto.RegisterRequest;
import org.example.popitkan5.dto.UpdateUserRequest;
import org.example.popitkan5.model.User;
import org.example.popitkan5.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("Register endpoint called with username: " + request.getUsername());
        try {
            AuthResponse response = authService.register(request);
            System.out.println("Registration successful for: " + request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Registration error for " + request.getUsername() + ": " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        System.out.println("Login endpoint called with username: " + request.getUsername());
        try {
            AuthResponse response = authService.authenticate(request);
            System.out.println("Login successful for: " + request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Login error for " + request.getUsername() + ": " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(authService.updateUser(authentication.getName(), request));
    }
}