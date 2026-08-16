package com.jachwisunbae.checklist.controller.dto;

import java.util.List;

import com.jachwisunbae.checklist.service.dto.UpdateUserChecklistCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserChecklistRequest(
        @NotBlank @Size(max = 50) String name,
        @NotEmpty @Size(max = 100) List<@NotNull Long> checkItemIds) {

    public UpdateUserChecklistCommand toCommand() {
        return new UpdateUserChecklistCommand(name, checkItemIds);
    }
}
