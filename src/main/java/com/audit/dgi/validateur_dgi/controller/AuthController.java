package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.dto.auth.AuthRequest;
import com.audit.dgi.validateur_dgi.dto.auth.AuthResponse;
import com.audit.dgi.validateur_dgi.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest req) {
        AuthResponse response = authService.registerAndGetResponse(req.getEmail(), req.getPassword(), "Company for " + req.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        AuthResponse response = authService.loginAndGetResponse(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(response);
    }
}

