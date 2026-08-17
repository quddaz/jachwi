package com.jachwisunbae.property.service.dto;

public record FieldUpdate<T>(boolean present, T value) {

    public static <T> FieldUpdate<T> omitted() {
        return new FieldUpdate<>(false, null);
    }

    public static <T> FieldUpdate<T> of(T value) {
        return new FieldUpdate<>(true, value);
    }

    public T apply(T current) {
        return present ? value : current;
    }
}
