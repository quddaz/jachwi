package com.jachwisunbae.checklist.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.jachwisunbae.checklist.controller.dto.CreateUserChecklistRequest;
import com.jachwisunbae.checklist.controller.dto.UserChecklistDetailResponse;
import com.jachwisunbae.checklist.service.UserChecklistService;
import com.jachwisunbae.checklist.service.dto.CreateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UserChecklistDetailResult;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.web.SuccessResponse;

class UserChecklistControllerTest {

    private final UserChecklistService service = mock(UserChecklistService.class);
    private final UserChecklistController controller = new UserChecklistController(service);

    @Test
    void createReturnsCreatedLocationAndResponse() {
        CreateUserChecklistRequest request = new CreateUserChecklistRequest(
                1L, "현장 체크", Stage.ON_SITE, List.of(10L));
        when(service.create(
                1L,
                new CreateUserChecklistCommand("현장 체크", Stage.ON_SITE, List.of(10L))))
                .thenReturn(new UserChecklistDetailResult(
                        100L, "현장 체크", Stage.ON_SITE, List.of()));

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/checklists/100");
        SuccessResponse<UserChecklistDetailResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.data().checklistId()).isEqualTo(100L);
    }
}
