package com.jachwisunbae.checklist.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import org.junit.jupiter.api.Test;

import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.common.exception.BusinessException;

class SystemCheckItemTest {

    @Test
    void createBuildsAnActiveItemWithoutAnId() {
        SystemCheckItem item = SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.CORE,
                "보일러가 정상적으로 작동하는가?",
                "온수와 난방을 직접 확인합니다.");

        assertThat(item.getId()).isNull();
        assertThat(item.getStage()).isEqualTo(Stage.ON_SITE);
        assertThat(item.getItemType()).isEqualTo(ItemType.CORE);
        assertThat(item.getQuestion()).isEqualTo("보일러가 정상적으로 작동하는가?");
        assertThat(item.getGuide()).isEqualTo("온수와 난방을 직접 확인합니다.");
        assertThat(item.isActive()).isTrue();
    }

    @Test
    void createRejectsBlankQuestion() {
        assertThatThrownBy(() -> SystemCheckItem.create(
                Stage.ON_SITE,
                ItemType.OPTIONAL,
                "  ",
                null))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo(DomainErrorCode.SYSTEM_CHECK_ITEM_QUESTION_INVALID);
    }

    @Test
    void deactivateChangesTheActiveState() {
        SystemCheckItem item = SystemCheckItem.restore(
                101L,
                Stage.ON_SITE,
                ItemType.CORE,
                "보일러가 정상적으로 작동하는가?",
                null,
                true);
        item.deactivate();

        assertThat(item.isActive()).isFalse();
    }
}
