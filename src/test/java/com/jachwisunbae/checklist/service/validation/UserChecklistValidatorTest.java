package com.jachwisunbae.checklist.service.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

class UserChecklistValidatorTest {

    private final UserChecklistValidator validator = new UserChecklistValidator();

    @Test
    void rejectsDuplicateItemIds() {
        assertThatThrownBy(() -> validator.validateItemIds(List.of(1L, 1L), false))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getCode())
                                .isEqualTo(DomainErrorCode.CHECKLIST_ITEMS_INVALID));
    }

    @Test
    void allowsInactiveItemOnlyWhenItAlreadyBelongsToChecklist() {
        SystemCheckItem inactive = SystemCheckItem.restore(
                1L, Stage.ON_SITE, ItemType.OPTIONAL, "질문", null, false);

        validator.validateItems(Stage.ON_SITE, List.of(inactive), Set.of(1L));

        assertThatThrownBy(() -> validator.validateItems(
                Stage.ON_SITE, List.of(inactive), Set.of()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getCode())
                                .isEqualTo(DomainErrorCode.CHECKLIST_INACTIVE_ITEM_NOT_ALLOWED));
    }
}
