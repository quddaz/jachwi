package com.jachwisunbae.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.repository.SystemCheckItemRepository;
import com.jachwisunbae.checklist.service.dto.SystemCheckItemResult;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;

class SystemCheckItemServiceTest {

    private final SystemCheckItemRepository repository = mock(SystemCheckItemRepository.class);
    private final SystemCheckItemService service = new SystemCheckItemService(repository);

    @Test
    void searchNormalizesQueryAndMapsItems() {
        SystemCheckItem item = SystemCheckItem.restore(
                101L,
                Stage.ON_SITE,
                ItemType.CORE,
                "보일러가 정상적으로 작동하는가?",
                "온수와 난방을 직접 확인합니다.",
                true);
        when(repository.findActive(Stage.ON_SITE, ItemType.CORE, "보일러"))
                .thenReturn(List.of(item));

        List<SystemCheckItemResult> result = service.search(
                Stage.ON_SITE,
                ItemType.CORE,
                "  보일러  ");

        assertThat(result).singleElement().satisfies(found -> {
            assertThat(found.checkItemId()).isEqualTo(101L);
            assertThat(found.question()).isEqualTo("보일러가 정상적으로 작동하는가?");
        });
    }
}
