package com.jachwisunbae.auth.controller;

import org.springframework.web.bind.annotation.*;
import com.jachwisunbae.auth.controller.dto.GoogleLoginRequest;
import com.jachwisunbae.auth.controller.dto.LoginResponse;
import com.jachwisunbae.auth.service.AuthService;
import com.jachwisunbae.common.web.SuccessResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }
    @PostMapping("/google")
    public SuccessResponse<LoginResponse> login(@Valid @RequestBody GoogleLoginRequest request) {
        return SuccessResponse.of(service.login(request));
    }
}
