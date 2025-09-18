package com.musinsa.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.category.dto.TokenRequest;
import com.musinsa.category.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 단위 테스트")
class AuthControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("토큰 발급 테스트")
    class IssueTokenTests {

        @Test
        @DisplayName("유효한 관리자 계정으로 토큰 발급 성공")
        void issueToken_WithValidCredentials_ShouldReturnToken() throws Exception {
            // given
            TokenRequest request = new TokenRequest();
            request.setAdminId("admin");
            request.setPassword("musinsa2025!");

            String expectedToken = "test.jwt.token";
            Date expectedExpiresAt = new Date(System.currentTimeMillis() + 3600000);

            given(jwtUtil.generateToken("admin")).willReturn(expectedToken);
            given(jwtUtil.getExpirationDate(expectedToken)).willReturn(expectedExpiresAt);

            // when & then
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("토큰이 성공적으로 발급되었습니다")))
                    .andExpect(jsonPath("$.data.token", is(expectedToken)))
                    .andExpect(jsonPath("$.data.adminId", is("admin")))
                    .andExpect(jsonPath("$.data.expiresAt", notNullValue()));

            verify(jwtUtil).generateToken("admin");
            verify(jwtUtil).getExpirationDate(expectedToken);
        }

        @Test
        @DisplayName("모든 유효한 관리자 계정으로 토큰 발급 성공")
        void issueToken_WithAllValidAccounts_ShouldSucceed() throws Exception {
            // given
            Map<String, String> validAccounts = Map.of(
                    "admin", "musinsa2025!",
                    "category", "category123!",
                    "system", "system"
            );

            String expectedToken = "test.jwt.token";
            Date expectedExpiresAt = new Date();

            given(jwtUtil.generateToken(anyString())).willReturn(expectedToken);
            given(jwtUtil.getExpirationDate(anyString())).willReturn(expectedExpiresAt);

            // when & then
            for (Map.Entry<String, String> account : validAccounts.entrySet()) {
                TokenRequest request = new TokenRequest();
                request.setAdminId(account.getKey());
                request.setPassword(account.getValue());

                mockMvc.perform(post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.data.adminId", is(account.getKey())));
            }
        }

        @Test
        @DisplayName("잘못된 관리자 ID로 토큰 발급 실패")
        void issueToken_WithInvalidAdminId_ShouldThrowException() throws Exception {
            // given
            TokenRequest request = new TokenRequest();
            request.setAdminId("invalid");
            request.setPassword("password");

            // when & then
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 토큰 발급 실패")
        void issueToken_WithInvalidPassword_ShouldThrowException() throws Exception {
            // given
            TokenRequest request = new TokenRequest();
            request.setAdminId("admin");
            request.setPassword("wrongpassword");

            // when & then
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("빈 요청으로 토큰 발급 실패")
        void issueToken_WithEmptyRequest_ShouldReturnBadRequest() throws Exception {
            // given
            TokenRequest request = new TokenRequest();

            // when & then
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("null 값으로 토큰 발급 실패")
        void issueToken_WithNullValues_ShouldReturnBadRequest() throws Exception {
            // given
            TokenRequest request = new TokenRequest();
            request.setAdminId(null);
            request.setPassword(null);

            // when & then
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class VerifyTokenTests {

        @Test
        @DisplayName("유효한 토큰으로 검증 성공")
        void verifyToken_WithValidToken_ShouldReturnTokenInfo() throws Exception {
            // given
            String token = "valid.jwt.token";
            String authHeader = "Bearer " + token;
            String adminId = "admin";
            Date expiresAt = new Date();

            given(jwtUtil.extractTokenFromHeader(authHeader)).willReturn(token);
            given(jwtUtil.isTokenValid(token)).willReturn(true);
            given(jwtUtil.extractAdminId(token)).willReturn(adminId);
            given(jwtUtil.getExpirationDate(token)).willReturn(expiresAt);

            // when & then
            mockMvc.perform(get("/auth/verify")
                            .header("Authorization", authHeader))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("유효한 토큰입니다")))
                    .andExpect(jsonPath("$.data.adminId", is(adminId)))
                    .andExpect(jsonPath("$.data.valid", is(true)))
                    .andExpect(jsonPath("$.data.expiresAt", notNullValue()));

            verify(jwtUtil).extractTokenFromHeader(authHeader);
            verify(jwtUtil).isTokenValid(token);
            verify(jwtUtil).extractAdminId(token);
            verify(jwtUtil).getExpirationDate(token);
        }

        @Test
        @DisplayName("Authorization 헤더 없이 토큰 검증 실패")
        void verifyToken_WithoutAuthHeader_ShouldThrowException() throws Exception {
            // given
            given(jwtUtil.extractTokenFromHeader(null)).willReturn(null);

            // when & then
            mockMvc.perform(get("/auth/verify"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil).extractTokenFromHeader(null);
            verify(jwtUtil, never()).isTokenValid(anyString());
        }

        @Test
        @DisplayName("빈 Authorization 헤더로 토큰 검증 실패")
        void verifyToken_WithEmptyAuthHeader_ShouldThrowException() throws Exception {
            // given
            String emptyHeader = "";
            given(jwtUtil.extractTokenFromHeader(emptyHeader)).willReturn(null);

            // when & then
            mockMvc.perform(get("/auth/verify")
                            .header("Authorization", emptyHeader))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil).extractTokenFromHeader(emptyHeader);
        }

        @Test
        @DisplayName("잘못된 형식의 Authorization 헤더로 토큰 검증 실패")
        void verifyToken_WithInvalidAuthHeader_ShouldThrowException() throws Exception {
            // given
            String invalidHeader = "Invalid token";
            given(jwtUtil.extractTokenFromHeader(invalidHeader)).willReturn(null);

            // when & then
            mockMvc.perform(get("/auth/verify")
                            .header("Authorization", invalidHeader))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 검증 실패")
        void verifyToken_WithInvalidToken_ShouldThrowException() throws Exception {
            // given
            String invalidToken = "invalid.jwt.token";
            String authHeader = "Bearer " + invalidToken;

            given(jwtUtil.extractTokenFromHeader(authHeader)).willReturn(invalidToken);
            given(jwtUtil.isTokenValid(invalidToken)).willReturn(false);

            // when & then
            mockMvc.perform(get("/auth/verify")
                            .header("Authorization", authHeader))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil).extractTokenFromHeader(authHeader);
            verify(jwtUtil).isTokenValid(invalidToken);
            verify(jwtUtil, never()).extractAdminId(anyString());
        }

        @Test
        @DisplayName("만료된 토큰으로 검증 실패")
        void verifyToken_WithExpiredToken_ShouldThrowException() throws Exception {
            // given
            String expiredToken = "expired.jwt.token";
            String authHeader = "Bearer " + expiredToken;

            given(jwtUtil.extractTokenFromHeader(authHeader)).willReturn(expiredToken);
            given(jwtUtil.isTokenValid(expiredToken)).willReturn(false);

            // when & then
            mockMvc.perform(get("/auth/verify")
                            .header("Authorization", authHeader))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil).isTokenValid(expiredToken);
        }
    }

    @Nested
    @DisplayName("관리자 계정 검증 테스트")
    class AdminValidationTests {

        @Test
        @DisplayName("모든 유효한 관리자 계정 검증")
        void validateAllAdminAccounts() throws Exception {
            // given
            Map<String, String> accounts = Map.of(
                    "admin", "musinsa2025!",
                    "category", "category123!",
                    "system", "system"
            );

            String token = "test.token";
            Date expiresAt = new Date();

            given(jwtUtil.generateToken(anyString())).willReturn(token);
            given(jwtUtil.getExpirationDate(anyString())).willReturn(expiresAt);

            // when & then
            for (Map.Entry<String, String> account : accounts.entrySet()) {
                TokenRequest request = new TokenRequest();
                request.setAdminId(account.getKey());
                request.setPassword(account.getValue());

                mockMvc.perform(post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)));
            }
        }

        @Test
        @DisplayName("존재하지 않는 관리자 계정으로 검증 실패")
        void validateNonExistentAdmin_ShouldFail() throws Exception {
            // given
            TokenRequest request = new TokenRequest();
            request.setAdminId("nonexistent");
            request.setPassword("password");

            // when & then
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("대소문자 구분하여 관리자 계정 검증")
        void validateAdminWithCaseSensitive_ShouldFail() throws Exception {
            // given
            TokenRequest request = new TokenRequest();
            request.setAdminId("ADMIN"); // 대문자
            request.setPassword("musinsa2025!");

            // when & then
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}