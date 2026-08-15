package com.jachwisunbae.checklist.controller.dto;

import java.util.List;

import com.jachwisunbae.checklist.service.dto.CreateUserChecklistCommand;
import com.jachwisunbae.checklist.type.Stage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserChecklistRequest(
        @NotNull Long memberId,
        @NotBlank @Size(max = 50) String name,
        @NotNull Stage stage,
        @NotNull @Size(max = 100) List<@NotNull Long> checkItemIds) {

    public CreateUserChecklistCommand toCommand() {
        return new CreateUserChecklistCommand(name, stage, checkItemIds);
    }
}
