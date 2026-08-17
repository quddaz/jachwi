package com.jachwisunbae.property.memo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jachwisunbae.auth.web.AuthenticatedMemberId;
import com.jachwisunbae.common.web.SuccessResponse;
import com.jachwisunbae.property.memo.controller.dto.PropertyMemoResponse;
import com.jachwisunbae.property.memo.controller.dto.ReplacePropertyMemoRequest;
import com.jachwisunbae.property.memo.service.PropertyMemoService;

@RestController
@RequestMapping("/api/properties/{propertyId}/memo")
public class PropertyMemoController {

    private final PropertyMemoService service;

    public PropertyMemoController(PropertyMemoService service) {
        this.service = service;
    }

    @GetMapping
    public SuccessResponse<PropertyMemoResponse> get(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId) {
        return SuccessResponse.of(PropertyMemoResponse.from(service.get(memberId, propertyId)));
    }

    @PutMapping
    public SuccessResponse<PropertyMemoResponse> replace(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId,
            @RequestBody ReplacePropertyMemoRequest request) {
        return SuccessResponse.of(PropertyMemoResponse.from(
                service.replace(memberId, propertyId, request.toCommand())));
    }
}
