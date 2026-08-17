package com.jachwisunbae.property.memo.service.dto;

import java.util.List;

public record ReplacePropertyMemoCommand(List<Item> items, String freeMemo) {

    public record Item(String label, String content) {
    }
}
