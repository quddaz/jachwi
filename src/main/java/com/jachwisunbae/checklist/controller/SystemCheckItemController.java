package com.jachwisunbae.checklist.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jachwisunbae.checklist.controller.dto.SystemCheckItemResponse;
import com.jachwisunbae.checklist.service.SystemCheckItemService;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.common.web.SuccessResponse;

import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/check-items")
public class SystemCheckItemController {

    private final SystemCheckItemService systemCheckItemService;

    public SystemCheckItemController(SystemCheckItemService systemCheckItemService) {
        this.systemCheckItemService = systemCheckItemService;
    }

    @GetMapping
    public SuccessResponse<List<SystemCheckItemResponse>> getCheckItems(
            @RequestParam Stage stage,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) @Size(max = 50) String query) {
        List<SystemCheckItemResponse> response = systemCheckItemService.search(stage, type, query)
                .stream()
                .map(SystemCheckItemResponse::from)
                .toList();
        return SuccessResponse.of(response);
    }
}
