package com.jachwisunbae.member.service.dto;

import com.jachwisunbae.member.entity.Member;

public record MemberResult(Long memberId, String name, String email) {

    public static MemberResult from(Member member) {
        return new MemberResult(member.getId(), member.getName(), member.getEmail());
    }
}
