package com.jachwisunbae.checklist.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jachwisunbae.checklist.controller.dto.CreateUserChecklistRequest;
import com.jachwisunbae.checklist.controller.dto.UpdateUserChecklistRequest;
import com.jachwisunbae.checklist.controller.dto.UserChecklistDetailResponse;
import com.jachwisunbae.checklist.controller.dto.UserChecklistSummaryResponse;
import com.jachwisunbae.checklist.service.UserChecklistService;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.web.SuccessResponse;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/checklists")
public class UserChecklistController {

    private final UserChecklistService userChecklistService;

    public UserChecklistController(UserChecklistService userChecklistService) {
        this.userChecklistService = userChecklistService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<UserChecklistDetailResponse>> create(
            @Valid @RequestBody CreateUserChecklistRequest request) {
        UserChecklistDetailResponse response = UserChecklistDetailResponse.from(
                userChecklistService.create(request.memberId(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/checklists/" + response.checklistId()))
                .body(SuccessResponse.of(response));
    }

    @GetMapping
    public SuccessResponse<List<UserChecklistSummaryResponse>> findAll(
            @RequestParam Long memberId,
            @RequestParam(required = false) Stage stage) {
        return SuccessResponse.of(userChecklistService.findAll(memberId, stage).stream()
                .map(UserChecklistSummaryResponse::from)
                .toList());
    }

    @GetMapping("/{checklistId}")
    public SuccessResponse<UserChecklistDetailResponse> findById(
            @RequestParam Long memberId,
            @PathVariable Long checklistId) {
        return SuccessResponse.of(UserChecklistDetailResponse.from(
                userChecklistService.findById(memberId, checklistId)));
    }

    @PutMapping("/{checklistId}")
    public SuccessResponse<UserChecklistDetailResponse> update(
            @PathVariable Long checklistId,
            @Valid @RequestBody UpdateUserChecklistRequest request) {
        return SuccessResponse.of(UserChecklistDetailResponse.from(
                userChecklistService.update(request.memberId(), checklistId, request.toCommand())));
    }

    @DeleteMapping("/{checklistId}")
    public ResponseEntity<Void> delete(
            @RequestParam Long memberId,
            @PathVariable Long checklistId) {
        userChecklistService.delete(memberId, checklistId);
        return ResponseEntity.noContent().build();
    }
}
