package com.jachwisunbae.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record RotateTokenRequest(@NotBlank String refreshToken) {
}
