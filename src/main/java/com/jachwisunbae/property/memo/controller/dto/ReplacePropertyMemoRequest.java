package com.jachwisunbae.property.memo.controller.dto;

import java.util.List;

import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;

public record ReplacePropertyMemoRequest(List<ItemRequest> items, String freeMemo) {

    public ReplacePropertyMemoCommand toCommand() {
        return new ReplacePropertyMemoCommand(
                items == null ? null : items.stream().map(ItemRequest::toCommand).toList(),
                freeMemo);
    }

    public record ItemRequest(String label, String content) {
        private ReplacePropertyMemoCommand.Item toCommand() {
            return new ReplacePropertyMemoCommand.Item(label, content);
        }
    }
}
