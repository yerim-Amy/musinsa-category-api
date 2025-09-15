package com.musinsa.category.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 카테고리 관련 에러
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "카테고리를 찾을 수 없습니다"),
    CATEGORY_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "C002", "하위 카테고리가 존재하여 삭제할 수 없습니다"),
    CATEGORY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "C003", "카테고리 깊이 제한을 초과했습니다"),
    CATEGORY_PARENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "C004", "부모 카테고리를 찾을 수 없습니다"),
    CATEGORY_INACTIVE_PARENT(HttpStatus.BAD_REQUEST, "C005", "부모 카테고리가 비활성화되어 있습니다"),
    INVALID_CATEGORY_OPERATION(HttpStatus.BAD_REQUEST, "C006", "잘못된 카테고리 작업입니다"),
    CATEGORY_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "C007", "중복된 카테고리명이 있습니다"),
    CATEGORY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "C008", "카테고리 이름은 필수 입니다"),
    CATEGORY_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "C009", "카테고리 이름이 100자를 넘습니다"),
    INVALID_DISPLAY_ORDER(HttpStatus.BAD_REQUEST, "C009", "정렬 순서는 0 이상이어야 합니다"),

    // 인증 관련 에러
    INVALID_ADMIN_CREDENTIALS(HttpStatus.UNAUTHORIZED,"A001", "잘못된 계정 정보입니다"),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED,"A002", "토큰이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"A003", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"A004", "만료된 토큰입니다"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "A005", "접근 권한이 없습니다"),
    // 일반적인 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G001", "잘못된 입력값입니다"),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "G002", "잘못된 인수값입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G003", "서버 내부 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "G004", "지원하지 않는 HTTP 메서드입니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}