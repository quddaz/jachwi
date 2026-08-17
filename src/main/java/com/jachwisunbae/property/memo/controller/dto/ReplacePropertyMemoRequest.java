package com.jachwisunbae.property.memo.controller.dto;

import java.util.List;
import java.util.ArrayList;

import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;
import com.jachwisunbae.property.memo.service.dto.PropertyMemoItemCommand;

public record ReplacePropertyMemoRequest(
        List<PropertyMemoItemRequest> items,
        String freeMemo) {

    public ReplacePropertyMemoCommand toCommand() {
        return new ReplacePropertyMemoCommand(
                toItemCommands(),
                freeMemo);
    }

    private List<PropertyMemoItemCommand> toItemCommands() {
        if (items == null) {
            return null;
        }
        List<PropertyMemoItemCommand> commands = new ArrayList<>();
        for (PropertyMemoItemRequest item : items) {
            commands.add(item.toCommand());
        }
        return List.copyOf(commands);
    }
}
