package com.jachwisunbae.property.checklist.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jachwisunbae.auth.web.AuthenticatedMemberId;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.web.SuccessResponse;
import com.jachwisunbae.property.checklist.controller.dto.ApplyPropertyChecklistRequest;
import com.jachwisunbae.property.checklist.controller.dto.PropertyChecklistDetailResponse;
import com.jachwisunbae.property.checklist.controller.dto.PropertyChecklistSummaryResponse;
import com.jachwisunbae.property.checklist.controller.dto.UpdateCheckMemoRequest;
import com.jachwisunbae.property.checklist.controller.dto.UpdateCheckStatusRequest;
import com.jachwisunbae.property.checklist.service.PropertyChecklistService;

@RestController
@RequestMapping("/api/properties/{propertyId}/checklists")
public class PropertyChecklistController {

    private final PropertyChecklistService service;

    public PropertyChecklistController(PropertyChecklistService service) {
        this.service = service;
    }

    @GetMapping
    public SuccessResponse<List<PropertyChecklistSummaryResponse>> findAll(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId) {
        return SuccessResponse.of(service.findAll(memberId, propertyId).stream()
                .map(PropertyChecklistSummaryResponse::from)
                .toList());
    }

    @PutMapping("/{stage}")
    public SuccessResponse<PropertyChecklistDetailResponse> applyOrReplace(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId,
            @PathVariable Stage stage,
            @RequestBody ApplyPropertyChecklistRequest request) {
        return SuccessResponse.of(PropertyChecklistDetailResponse.from(
                service.applyOrReplace(memberId, propertyId, stage, request.userChecklistId())));
    }

    @GetMapping("/{propertyChecklistId}")
    public SuccessResponse<PropertyChecklistDetailResponse> findOne(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId,
            @PathVariable Long propertyChecklistId) {
        return SuccessResponse.of(PropertyChecklistDetailResponse.from(
                service.findOne(memberId, propertyId, propertyChecklistId)));
    }

    @PatchMapping("/{propertyChecklistId}/items/{itemId}/status")
    public ResponseEntity<Void> updateStatus(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId,
            @PathVariable Long propertyChecklistId,
            @PathVariable Long itemId,
            @RequestBody UpdateCheckStatusRequest request) {
        service.updateStatus(
                memberId, propertyId, propertyChecklistId, itemId, request.status());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{propertyChecklistId}/items/{itemId}/memo")
    public ResponseEntity<Void> updateMemo(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long propertyId,
            @PathVariable Long propertyChecklistId,
            @PathVariable Long itemId,
            @RequestBody UpdateCheckMemoRequest request) {
        service.updateMemo(memberId, propertyId, propertyChecklistId, itemId, request.memo());
        return ResponseEntity.noContent().build();
    }
}
