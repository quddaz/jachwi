package com.jachwisunbae.property.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.jachwisunbae.property.controller.dto.CreatePropertyRequest;
import com.jachwisunbae.property.service.PropertyService;
import com.jachwisunbae.property.service.dto.CheckProgressResult;
import com.jachwisunbae.property.service.dto.PropertyResult;

class PropertyControllerTest {

    private final PropertyService service = mock(PropertyService.class);
    private final PropertyController controller = new PropertyController(service);

    @Test
    void createReturnsCreatedLocation() {
        CreatePropertyRequest request = new CreatePropertyRequest(
                "신림 원룸", null, 0L, null, null, null);
        when(service.create(1L, request.toCommand())).thenReturn(new PropertyResult(
                10L, "신림 원룸", null, 0L, null, null, null,
                null, null, null, CheckProgressResult.of(0, 0)));

        var response = controller.create(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/properties/10");
        assertThat(response.getBody().data().propertyId()).isEqualTo(10L);
    }
}
