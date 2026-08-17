package com.jachwisunbae.property.checklist.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.property.checklist.controller.dto.ApplyPropertyChecklistRequest;
import com.jachwisunbae.property.checklist.service.PropertyChecklistService;
import com.jachwisunbae.property.checklist.service.dto.CheckProgressResult;
import com.jachwisunbae.property.checklist.service.dto.PropertyChecklistDetailResult;

class PropertyChecklistControllerTest {

    private final PropertyChecklistService service = mock(PropertyChecklistService.class);
    private final PropertyChecklistController controller = new PropertyChecklistController(service);

    @Test
    void appliesChecklistForRequestedStage() {
        when(service.applyOrReplace(1L, 10L, Stage.ON_SITE, 20L))
                .thenReturn(new PropertyChecklistDetailResult(
                        30L, 20L, "현장 체크", Stage.ON_SITE, List.of(),
                        new CheckProgressResult(0, 0, 0, 0, 0, 0)));

        var response = controller.applyOrReplace(
                1L, 10L, Stage.ON_SITE, new ApplyPropertyChecklistRequest(20L));

        assertThat(response.data().propertyChecklistId()).isEqualTo(30L);
        assertThat(response.data().stage()).isEqualTo(Stage.ON_SITE);
    }
}
