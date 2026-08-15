package com.jachwisunbae.member.entity;

import static com.jachwisunbae.common.validation.DomainPreconditions.requireNonBlank;
import static com.jachwisunbae.common.validation.DomainPreconditions.requireNonNull;

import java.time.LocalDateTime;

import com.jachwisunbae.common.exception.DomainErrorCode;

import lombok.Getter;

@Getter
public class Member {

    private final Long id;
    private final String subject;
    private String email;
    private String name;
    private LocalDateTime lastLoginAt;

    private Member(
            Long id,
            String subject,
            String email,
            String name,
            LocalDateTime lastLoginAt) {
        this.id = id;
        this.subject = subject;
        this.email = email;
        this.name = name;
        this.lastLoginAt = lastLoginAt;
    }

    public static Member create(
            String subject,
            String email,
            String name,
            LocalDateTime lastLoginAt) {
        return new Member(
                null,
                requireNonBlank(
                        subject,
                        DomainErrorCode.MEMBER_SUBJECT_INVALID,
                        "회원 subject는 필수입니다."),
                requireNonBlank(
                        email,
                        DomainErrorCode.MEMBER_EMAIL_INVALID,
                        "이메일은 필수입니다."),
                requireNonBlank(
                        name,
                        DomainErrorCode.MEMBER_NAME_INVALID,
                        "회원 이름은 필수입니다."),
                requireNonNull(
                        lastLoginAt,
                        DomainErrorCode.MEMBER_LAST_LOGIN_AT_REQUIRED,
                        "마지막 로그인 시각은 필수입니다."));
    }

    public static Member restore(
            Long id,
            String subject,
            String email,
            String name,
            LocalDateTime lastLoginAt) {
        return new Member(
                id,
                subject,
                email,
                name,
                lastLoginAt);
    }

    public void updateLoginProfile(
            String email,
            String name,
            LocalDateTime lastLoginAt) {
        this.email = requireNonBlank(
                email,
                DomainErrorCode.MEMBER_EMAIL_INVALID,
                "이메일은 필수입니다.");
        this.name = requireNonBlank(
                name,
                DomainErrorCode.MEMBER_NAME_INVALID,
                "회원 이름은 필수입니다.");
        this.lastLoginAt = requireNonNull(
                lastLoginAt,
                DomainErrorCode.MEMBER_LAST_LOGIN_AT_REQUIRED,
                "마지막 로그인 시각은 필수입니다.");
    }
}
