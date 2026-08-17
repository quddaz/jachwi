package com.jachwisunbae.property.service.dto;

import java.util.List;

public record PropertyPageResult(
        List<PropertyResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static PropertyPageResult of(
            List<PropertyResult> content,
            int page,
            int size,
            long totalElements) {
        int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
        return new PropertyPageResult(
                content, page, size, totalElements, totalPages, page + 1 < totalPages);
    }
}
