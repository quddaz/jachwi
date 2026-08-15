package com.jachwisunbae.checklist.service.dto;

import java.util.List;

public record UpdateUserChecklistCommand(String name, List<Long> checkItemIds) {
}
