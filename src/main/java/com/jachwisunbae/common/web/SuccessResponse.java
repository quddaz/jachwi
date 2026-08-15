package com.jachwisunbae.common.web;

public record SuccessResponse<T>(String code, String message, T data) {

    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>("SUCCESS", "요청이 성공했습니다.", data);
    }
}
