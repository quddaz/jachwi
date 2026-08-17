package com.jachwisunbae.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jachwisunbae.auth.web.AuthenticatedMemberId;
import com.jachwisunbae.common.web.SuccessResponse;
import com.jachwisunbae.member.controller.dto.MemberResponse;
import com.jachwisunbae.member.service.MemberService;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public SuccessResponse<MemberResponse> getMe(@AuthenticatedMemberId Long memberId) {
        return SuccessResponse.of(MemberResponse.from(service.getMe(memberId)));
    }
}
