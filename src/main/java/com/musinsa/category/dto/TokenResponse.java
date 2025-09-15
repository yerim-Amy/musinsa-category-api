package com.musinsa.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String token;           // JWT 토큰
    private String adminId;         // 관리자 ID
    private long expiresIn;         // 만료 시간 (초)
    private Date expiresAt;         // 만료 일시

    public static TokenResponse of(String token, String adminId, Date expiresAt) {
        long expiresIn = (expiresAt.getTime() - System.currentTimeMillis()) / 1000;

        return TokenResponse.builder()
                .token(token)
                .adminId(adminId)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .build();
    }
}