package com.jachwisunbae.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.member.repository.MemberRepository;
import com.jachwisunbae.member.service.dto.MemberResult;

@Service
public class MemberService {

    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public MemberResult getMe(Long memberId) {
        return repository.findById(memberId)
                .map(MemberResult::from)
                .orElseThrow(() -> new BusinessException(
                        DomainErrorCode.MEMBER_NOT_FOUND,
                        "현재 회원을 찾을 수 없습니다."));
    }
}
