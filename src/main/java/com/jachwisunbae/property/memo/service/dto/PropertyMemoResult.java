package com.jachwisunbae.property.memo.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PropertyMemoResult(List<Item> items, String freeMemo, LocalDateTime savedAt) {

    public static PropertyMemoResult empty() {
        return new PropertyMemoResult(List.of(), "", null);
    }

    public record Item(String label, String content, int order) {
    }
}
