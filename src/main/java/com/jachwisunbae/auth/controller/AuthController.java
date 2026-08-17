package com.jachwisunbae.auth.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jachwisunbae.auth.controller.dto.LoginResponse;
import com.jachwisunbae.auth.controller.dto.LogoutRequest;
import com.jachwisunbae.auth.controller.dto.OAuthLoginRequest;
import com.jachwisunbae.auth.controller.dto.RotateTokenRequest;
import com.jachwisunbae.auth.controller.dto.TokenResponse;
import com.jachwisunbae.auth.provider.OAuthProviderType;
import com.jachwisunbae.auth.service.AuthService;
import com.jachwisunbae.auth.web.AuthenticatedMemberId;
import com.jachwisunbae.common.web.SuccessResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/{provider}")
    public SuccessResponse<LoginResponse> login(
            @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request) {
        return SuccessResponse.of(service.login(OAuthProviderType.from(provider), request));
    }

    @PostMapping("/tokens")
    public SuccessResponse<TokenResponse> rotate(
            @Valid @RequestBody RotateTokenRequest request) {
        return SuccessResponse.of(service.rotate(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody LogoutRequest request) {
        service.logout(memberId, request.refreshToken());
    }
}
