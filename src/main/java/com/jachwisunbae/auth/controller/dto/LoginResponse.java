package com.jachwisunbae.auth.controller.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, MemberResponse member) {
    public record MemberResponse(Long memberId, String name, String email) {}
}
