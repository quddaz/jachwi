package com.jachwisunbae.property.memo.entity;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class PropertyMemo {

    private final Long id;
    private final Long propertyId;
    private final String freeMemo;
    private final LocalDateTime updatedAt;

    private PropertyMemo(Long id, Long propertyId, String freeMemo, LocalDateTime updatedAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.freeMemo = freeMemo;
        this.updatedAt = updatedAt;
    }

    public static PropertyMemo restore(
            Long id,
            Long propertyId,
            String freeMemo,
            LocalDateTime updatedAt) {
        return new PropertyMemo(id, propertyId, freeMemo, updatedAt);
    }
}
