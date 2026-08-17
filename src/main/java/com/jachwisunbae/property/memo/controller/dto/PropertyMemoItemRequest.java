package com.jachwisunbae.property.memo.controller.dto;

import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemCommand;

public record PropertyMemoItemRequest(String label, String content) {

    public PropertyMemoItemCommand toCommand() {
        return new PropertyMemoItemCommand(label, content);
    }
}
