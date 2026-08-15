package com.jachwisunbae.checklist.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.checklist.controller.dto.SystemCheckItemResponse;
import com.jachwisunbae.checklist.service.SystemCheckItemService;
import com.jachwisunbae.checklist.service.dto.SystemCheckItemResult;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.web.SuccessResponse;

class SystemCheckItemControllerTest {

    private final SystemCheckItemService service = mock(SystemCheckItemService.class);
    private final SystemCheckItemController controller = new SystemCheckItemController(service);

    @Test
    void getCheckItemsMapsServiceResultToHttpResponse() {
        List<SystemCheckItemResult> result = List.of(new SystemCheckItemResult(
                        101L,
                        Stage.ON_SITE,
                        ItemType.CORE,
                        "보일러가 정상적으로 작동하는가?",
                        "온수와 난방을 직접 확인합니다."));
        when(service.search(Stage.ON_SITE, ItemType.CORE, "보일러"))
                .thenReturn(result);

        SuccessResponse<List<SystemCheckItemResponse>> response = controller.getCheckItems(
                Stage.ON_SITE,
                ItemType.CORE,
                "보일러");

        assertThat(response.code()).isEqualTo("SUCCESS");
        assertThat(response.data()).singleElement().satisfies(item -> {
            assertThat(item.checkItemId()).isEqualTo(101L);
            assertThat(item.type()).isEqualTo(ItemType.CORE);
        });
    }
}
