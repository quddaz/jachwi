package com.jachwisunbae.property.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jachwisunbae.auth.web.AuthenticatedMemberId;
import com.jachwisunbae.common.web.PageResponse;
import com.jachwisunbae.common.web.SuccessResponse;
import com.jachwisunbae.property.controller.dto.CreatePropertyRequest;
import com.jachwisunbae.property.controller.dto.PropertyResponse;
import com.jachwisunbae.property.controller.dto.UpdatePropertyRequest;
import com.jachwisunbae.property.service.PropertyService;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService service;

    public PropertyController(PropertyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<PropertyResponse>> create(
            @AuthenticatedMemberId Long memberId,
            @RequestBody CreatePropertyRequest request) {
        PropertyResponse response = PropertyResponse.from(
                service.create(memberId, request.toCommand()));
        return ResponseEntity.created(URI.create("/api/properties/" + response.propertyId()))
                .body(SuccessResponse.of(response));
    }

    @GetMapping
    public SuccessResponse<PageResponse<PropertyResponse>> findAll(
            @AuthenticatedMemberId Long memberId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = service.findAll(memberId, query, page, size);
        return SuccessResponse.of(new PageResponse<>(
                result.content().stream().map(PropertyResponse::from).toList(),
                result.page(), result.size(), result.totalElements(),
                result.totalPages(), result.hasNext()));
    }

    @GetMapping("/{propertyId}")
    public SuccessResponse<PropertyResponse> findOne(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId) {
        return SuccessResponse.of(PropertyResponse.from(service.findOne(memberId, propertyId)));
    }

    @PatchMapping("/{propertyId}")
    public SuccessResponse<PropertyResponse> update(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId,
            @RequestBody UpdatePropertyRequest request) {
        return SuccessResponse.of(PropertyResponse.from(
                service.update(memberId, propertyId, request.toCommand())));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> delete(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId) {
        service.delete(memberId, propertyId);
        return ResponseEntity.noContent().build();
    }
}
