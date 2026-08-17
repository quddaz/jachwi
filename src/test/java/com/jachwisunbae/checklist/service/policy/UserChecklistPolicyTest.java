package com.jachwisunbae.checklist.service.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;

class UserChecklistPolicyTest {

    private final UserChecklistPolicy policy = new UserChecklistPolicy();

    @Test
    void activeCoreItemsComeBeforeRequestedOptionalItems() {
        SystemCheckItem core = SystemCheckItem.restore(
                10L, Stage.ON_SITE, ItemType.CORE, "핵심", null, true);

        assertThat(policy.resolveCreateItemIds(List.of(20L), List.of(core)))
                .containsExactly(10L, 20L);
    }

}
