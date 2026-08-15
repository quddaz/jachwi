package com.jachwisunbae.checklist.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

class UserChecklistTest {

    @Test
    void createsChecklistWithNormalizedName() {
        UserChecklist checklist = UserChecklist.create(1L, "  현장 체크  ", Stage.ON_SITE);

        assertThat(checklist.getId()).isNull();
        assertThat(checklist.getMemberId()).isEqualTo(1L);
        assertThat(checklist.getName()).isEqualTo("현장 체크");
        assertThat(checklist.getStage()).isEqualTo(Stage.ON_SITE);
        assertThat(checklist.getDeletedAt()).isNull();
        assertThat(checklist.isDeleted()).isFalse();
    }

    @Test
    void rejectsNameLongerThanFiftyCharacters() {
        assertThatThrownBy(() -> UserChecklist.create(1L, "가".repeat(51), Stage.ON_SITE))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.USER_CHECKLIST_NAME_INVALID);
    }

    @Test
    void changesNameWithoutChangingStage() {
        UserChecklist checklist = UserChecklist.create(1L, "기존 이름", Stage.PRE_CONTRACT);

        checklist.rename("  변경 이름  ");

        assertThat(checklist.getName()).isEqualTo("변경 이름");
        assertThat(checklist.getStage()).isEqualTo(Stage.PRE_CONTRACT);
    }

    @Test
    void marksChecklistAsDeleted() {
        UserChecklist checklist = UserChecklist.create(1L, "현장 체크", Stage.ON_SITE);
        LocalDateTime deletedAt = LocalDateTime.of(2026, 8, 15, 19, 30);

        checklist.delete(deletedAt);

        assertThat(checklist.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(checklist.isDeleted()).isTrue();
    }
}
