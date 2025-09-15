package com.musinsa.category.controller;

import com.musinsa.category.common.ApiResponse;
import com.musinsa.category.security.JwtUtil;
import com.musinsa.category.dto.TokenRequest;
import com.musinsa.category.dto.TokenResponse;
import com.musinsa.category.exception.BusinessException;
import com.musinsa.category.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "인증 관리 API")
public class AuthController {

    private final JwtUtil jwtUtil;

    // TODO 관리자 계정 (테스트용)
    private static final Map<String, String> ADMIN_ACCOUNTS = Map.of(
            "admin", "musinsa2025!",
            "category", "category123!",
            "system", "system");

    /**
     * 관리자 토큰 발급
     */
    @PostMapping("/token")
    @Operation(summary = "관리자 토큰 발급", description = "관리자용 JWT 토큰을 발급합니다")
    public ApiResponse<TokenResponse> issueToken(@Valid @RequestBody TokenRequest request) {
        log.info("토큰 발급 요청 - adminId: {}", request.getAdminId());

        // 계정 검증
        if (!isValidAdmin(request.getAdminId(), request.getPassword())) {
            log.warn("잘못된 관리자 계정 - adminId: {}", request.getAdminId());
            throw new BusinessException(ErrorCode.INVALID_ADMIN_CREDENTIALS);
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(request.getAdminId());
        Date expiresAt = jwtUtil.getExpirationDate(token);

        TokenResponse tokenResponse = TokenResponse.of(token, request.getAdminId(), expiresAt);

        log.info("토큰 발급 완료 - adminId: {}, expiresAt: {}", request.getAdminId(), expiresAt);
        return ApiResponse.success(tokenResponse, "토큰이 성공적으로 발급되었습니다");
    }

    /**
     * 토큰 검증 (테스트용)
     */
    @GetMapping("/verify")
    @Operation(summary = "토큰 검증", description = "현재 토큰의 유효성을 검증합니다")
    public ApiResponse<Map<String, Object>> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        if (token == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String adminId = jwtUtil.extractAdminId(token);
        Date expiresAt = jwtUtil.getExpirationDate(token);

        Map<String, Object> tokenInfo = Map.of("adminId", adminId,"expiresAt", expiresAt,"valid", true);

        return ApiResponse.success(tokenInfo, "유효한 토큰입니다");
    }

    /**
     * 관리자 계정 검증
     */
    private boolean isValidAdmin(String adminId, String password) {
        String expectedPassword = ADMIN_ACCOUNTS.get(adminId);
        return expectedPassword != null && expectedPassword.equals(password);
    }
}