package com.jachwisunbae.checklist.service.dto;

import java.util.List;

import com.jachwisunbae.checklist.type.Stage;

public record CreateUserChecklistCommand(String name, Stage stage, List<Long> checkItemIds) {
}
