package com.jachwisunbae.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.common.exception.BusinessException;

class MemberTest {

    private static final LocalDateTime LOGIN_AT = LocalDateTime.of(2026, 8, 15, 9, 0);

    @Test
    void createBuildsMemberWithoutDatabaseGeneratedId() {
        Member member = Member.create(
                "google-subject",
                "user@example.com",
                "이자취",
                LOGIN_AT);

        assertThat(member.getId()).isNull();
        assertThat(member.getSubject()).isEqualTo("google-subject");
        assertThat(member.getEmail()).isEqualTo("user@example.com");
        assertThat(member.getName()).isEqualTo("이자취");
        assertThat(member.getLastLoginAt()).isEqualTo(LOGIN_AT);
    }

    @Test
    void updateLoginProfileRefreshesVerifiedProfileAndLoginTime() {
        Member member = Member.restore(
                1L,
                "google-subject",
                "old@example.com",
                "이전 이름",
                LOGIN_AT);
        LocalDateTime nextLoginAt = LOGIN_AT.plusDays(1);

        member.updateLoginProfile("new@example.com", "새 이름", nextLoginAt);

        assertThat(member.getSubject()).isEqualTo("google-subject");
        assertThat(member.getEmail()).isEqualTo("new@example.com");
        assertThat(member.getName()).isEqualTo("새 이름");
        assertThat(member.getLastLoginAt()).isEqualTo(nextLoginAt);
    }

    @Test
    void createRejectsBlankName() {
        assertThatThrownBy(() -> Member.create(
                "google-subject",
                "user@example.com",
                "  ",
                LOGIN_AT))
                .isInstanceOfSatisfying(BusinessException.class, exception -> assertThat(exception.getCode())
                        .isEqualTo(DomainErrorCode.MEMBER_NAME_INVALID));
    }

    @Test
    void restoreTrustsValuesAlreadyReadFromTheDatabase() {
        Member member = Member.restore(
                0L,
                "google-subject",
                "user@example.com",
                "이자취",
                LOGIN_AT);

        assertThat(member.getId()).isZero();
    }
}
