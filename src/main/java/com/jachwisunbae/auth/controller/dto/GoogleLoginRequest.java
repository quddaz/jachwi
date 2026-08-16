package com.jachwisunbae.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String authorizationCode, @NotBlank String codeVerifier,
        @NotBlank String nonce, @NotBlank String redirectUri) {
}
