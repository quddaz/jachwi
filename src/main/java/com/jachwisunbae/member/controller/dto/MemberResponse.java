package com.jachwisunbae.member.controller.dto;

import com.jachwisunbae.member.service.dto.MemberResult;

public record MemberResponse(Long memberId, String name, String email) {

    public static MemberResponse from(MemberResult result) {
        return new MemberResponse(result.memberId(), result.name(), result.email());
    }
}
