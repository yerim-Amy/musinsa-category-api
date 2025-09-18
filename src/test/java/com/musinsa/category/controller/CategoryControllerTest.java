package com.musinsa.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.dto.CategoryResponse;
import com.musinsa.category.enums.Gender;
import com.musinsa.category.exception.BusinessException;
import com.musinsa.category.exception.ErrorCode;
import com.musinsa.category.security.JwtUtil;
import com.musinsa.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Unit Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtUtil jwtUtil;

    private String validToken;
    private String adminId;
    private CategoryResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validToken = "valid-jwt-token";
        adminId = "admin123";

        // JWT Mock 설정
        given(jwtUtil.extractTokenFromHeader("Bearer " + validToken))
                .willReturn(validToken);
        given(jwtUtil.isTokenValid(validToken))
                .willReturn(true);
        given(jwtUtil.extractAdminId(validToken))
                .willReturn(adminId);

        sampleResponse = createSampleResponse(1L, "상의", "상의 카테고리", null, 0, Gender.A, 1);
    }

    @Nested
    @DisplayName("POST /api/categories - 카테고리 생성")
    class CreateCategoryTests {

        @Test
        @DisplayName("성공 - 루트 카테고리 생성")
        void createRootCategory_Success() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("상의")
                    .description("상의 카테고리")
                    .gender(Gender.A)
                    .displayOrder(1)
                    .build();

            given(categoryService.createCategory(ArgumentMatchers.any(CategoryRequest.class), eq(adminId)))
                    .willReturn(sampleResponse);

            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("상의"))
                    .andExpect(jsonPath("$.data.description").value("상의 카테고리"))
                    .andExpect(jsonPath("$.data.gender").value("A"))
                    .andExpect(jsonPath("$.data.displayOrder").value(1))
                    .andExpect(jsonPath("$.data.depth").value(0))
                    .andExpect(jsonPath("$.data.parentId").doesNotExist());

            verify(categoryService).createCategory(ArgumentMatchers.any(CategoryRequest.class), eq(adminId));
        }

        @ParameterizedTest
        @EnumSource(Gender.class)
        @DisplayName("성공 - 모든 성별 타입으로 카테고리 생성")
        void createCategory_AllGenderTypes(Gender gender) throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트카테고리")
                    .gender(gender)
                    .displayOrder(1)
                    .build();

            CategoryResponse response = createSampleResponse(1L, "테스트카테고리", null, null, 0, gender, 1);
            given(categoryService.createCategory(ArgumentMatchers.any(CategoryRequest.class), eq(adminId)))
                    .willReturn(response);

            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.gender").value(gender.toString()));
        }

        @Test
        @DisplayName("성공 - 최대 깊이 자식 카테고리 생성")
        void createDeepChildCategory_Success() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("상세카테고리")
                    .parentId(3L)
                    .displayOrder(1)
                    .build();

            CategoryResponse deepResponse = createSampleResponse(4L, "상세카테고리", null, 3L, 3, Gender.A, 1);
            given(categoryService.createCategory(ArgumentMatchers.any(CategoryRequest.class), eq(adminId)))
                    .willReturn(deepResponse);
            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.depth").value(3))
                    .andExpect(jsonPath("$.data.parentId").value(3L));
        }

        @Test
        @DisplayName("실패 - 필수 필드 누락 (name)")
        void createCategory_FailWithMissingName() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .description("설명만 있음")
                    .build();

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(categoryService, never()).createCategory(any(), any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  ", "\n", "\t"})
        @DisplayName("실패 - 빈 카테고리명")
        void createCategory_FailWithEmptyName(String emptyName) throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name(emptyName)
                    .build();
            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(any(), any());
        }

        @Test
        @DisplayName("실패 - 카테고리명 길이 초과")
        void createCategory_FailWithTooLongName() throws Exception {
            String longName = "A".repeat(101); // 101자
            CategoryRequest request = CategoryRequest.builder()
                    .name(longName)
                    .build();

            given(categoryService.createCategory(ArgumentMatchers.any(CategoryRequest.class), eq(adminId)))
                    .willThrow(new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("G001"));
        }

        @Test
        @DisplayName("실패 - 음수 displayOrder")
        void createCategory_FailWithNegativeDisplayOrder() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트")
                    .displayOrder(-1)
                    .build();
            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(any(), any());
        }
    }

    @Nested
    @DisplayName("PUT /api/categories/{id} - 카테고리 수정")
    class UpdateCategoryTests {

        @Test
        @DisplayName("성공 - 부분 수정 (이름만)")
        void updateCategory_PartialUpdate() throws Exception {
            
            Long categoryId = 1L;
            CategoryRequest request = CategoryRequest.builder()
                    .name("수정된이름만")
                    .build();

            CategoryResponse updatedResponse = createSampleResponse(categoryId, "수정된이름만", "상의 카테고리", null, 0, Gender.A, 1);
            given(categoryService.updateCategory(eq(categoryId), ArgumentMatchers.any(CategoryRequest.class), eq(adminId)))
                    .willReturn(updatedResponse);

            
            mockMvc.perform(put("/api/categories/{id}", categoryId)
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("수정된이름만"));
        }

        @Test
        @DisplayName("성공 - 전체 수정")
        void updateCategory_FullUpdate() throws Exception {
            
            Long categoryId = 1L;
            CategoryRequest request = CategoryRequest.builder()
                    .name("완전히수정된상의")
                    .description("완전히 수정된 설명")
                    .gender(Gender.F)
                    .displayOrder(5)
                    .build();

            CategoryResponse updatedResponse = createSampleResponse(categoryId, "완전히수정된상의", "완전히 수정된 설명", null, 0, Gender.F, 5);
            given(categoryService.updateCategory(eq(categoryId), ArgumentMatchers.any(CategoryRequest.class), eq(adminId)))
                    .willReturn(updatedResponse);

            
            mockMvc.perform(put("/api/categories/{id}", categoryId)
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("완전히수정된상의"))
                    .andExpect(jsonPath("$.data.description").value("완전히 수정된 설명"))
                    .andExpect(jsonPath("$.data.gender").value("F"))
                    .andExpect(jsonPath("$.data.displayOrder").value(5));
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -999L})
        @DisplayName("실패 - 잘못된 카테고리 ID")
        void updateCategory_FailWithInvalidId(Long invalidId) throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("수정")
                    .build();

            mockMvc.perform(put("/api/categories/{id}", invalidId)
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).updateCategory(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/categories/{id} - 카테고리 삭제")
    class DeleteCategoryTests {

        @Test
        @DisplayName("성공 - 논리적 삭제")
        void deleteCategory_SoftDelete_Success() throws Exception {
            Long categoryId = 1L;
            doNothing().when(categoryService).deleteCategory(eq(categoryId), eq(adminId));

            mockMvc.perform(delete("/api/categories/{id}", categoryId)
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("카테고리가 성공적으로 삭제되었습니다"));

            verify(categoryService).deleteCategory(eq(categoryId), eq(adminId));
        }

        @Test
        @DisplayName("성공 - 물리적 삭제")
        void deleteCategory_HardDelete_Success() throws Exception {
            Long categoryId = 1L;
            doNothing().when(categoryService).realDeleteCategory(eq(categoryId), eq(adminId));
            
            mockMvc.perform(delete("/api/categories/{id}/real", categoryId)
                            .param("confirm", "true")
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("카테고리가 완전히 삭제되었습니다"));

            verify(categoryService).realDeleteCategory(eq(categoryId), eq(adminId));
        }

        @ParameterizedTest
        @ValueSource(strings = {"false", "FALSE", "", "no", "0"})
        @DisplayName("실패 - 잘못된 confirm 파라미터")
        void realDeleteCategory_FailWithInvalidConfirm(String confirmValue) throws Exception {
            mockMvc.perform(delete("/api/categories/{id}/real", 1L)
                            .param("confirm", confirmValue)
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("G001"));

            verify(categoryService, never()).realDeleteCategory(any(), any());
        }

        @Test
        @DisplayName("실패 - confirm 파라미터 누락")
        void realDeleteCategory_FailWithoutConfirmParam() throws Exception {
            mockMvc.perform(delete("/api/categories/{id}/real", 1L)
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).realDeleteCategory(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/categories - 카테고리 조회")
    class GetCategoryTests {

        @Test
        @DisplayName("성공 - 그냥 전체 조회")
        void getAllCategories_NoPaging() throws Exception {
            
            List<CategoryResponse> categories = Arrays.asList(
                    sampleResponse,
                    createSampleResponse(2L, "하의", "하의 카테고리", null, 0, Gender.A, 2)
            );
            given(categoryService.getAllCategories(Gender.A))
                    .willReturn(categories);

            mockMvc.perform(get("/api/categories")
                            .param("gender", "A"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name").value("상의"))
                    .andExpect(jsonPath("$.data[1].name").value("하의"));
        }

        @Test
        @DisplayName("성공 - 트리 구조 조회")
        void getCategoryTree_WithChildren() throws Exception {
            
            CategoryResponse parentCategory = createSampleResponse(1L, "상의", null, null, 0, Gender.A, 1);
            CategoryResponse childCategory = createSampleResponse(2L, "티셔츠", null, 1L, 1, Gender.A, 1);
            parentCategory.getChildren().add(childCategory);

            given(categoryService.getCategoryTree(null, Gender.A))
                    .willReturn(Arrays.asList(parentCategory));
            
            mockMvc.perform(get("/api/categories/tree")
                            .param("gender", "A"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].children", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].children[0].name").value("티셔츠"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"상", "상의", "티셔", "셔츠"})
        @DisplayName("성공 - 키워드로 검색")
        void searchCategories_VariousKeywords(String keyword) throws Exception {
            given(categoryService.searchCategories(keyword))
                    .willReturn(Arrays.asList(sampleResponse));

            mockMvc.perform(get("/api/categories/search")
                            .param("keyword", keyword))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            verify(categoryService).searchCategories(keyword);
        }

        @Test
        @DisplayName("성공 - 빈 문자 검색")
        void searchCategories_EmptyResult() throws Exception {
            given(categoryService.searchCategories("존재하지않음"))
                    .willReturn(Collections.emptyList());
            
            mockMvc.perform(get("/api/categories/search")
                            .param("keyword", "존재하지않음"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", empty()));
        }

        @Test
        @DisplayName("실패 - 검색 키워드 없는 경우")
        void searchCategories_FailWithoutKeyword() throws Exception {
            mockMvc.perform(get("/api/categories/search"))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).searchCategories(any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/categories/{id}/activate - 카테고리 활성화")
    class ActivateCategoryTests {

        @Test
        @DisplayName("성공 - 비활성화된 카테고리 활성화")
        void activateCategory_Success() throws Exception {
            Long categoryId = 1L;
            doNothing().when(categoryService).activateCategory(eq(categoryId), eq(adminId));

            mockMvc.perform(patch("/api/categories/{id}/activate", categoryId)
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("카테고리가 성공적으로 활성화되었습니다"));

            verify(categoryService).activateCategory(eq(categoryId), eq(adminId));
        }

        @Test
        @DisplayName("실패 - 이미 활성화된 카테고리")
        void activateCategory_FailAlreadyActive() throws Exception {
            Long categoryId = 1L;
            doThrow(new BusinessException(ErrorCode.CATEGORY_ALREADY_ACTIVE))
                    .when(categoryService).activateCategory(eq(categoryId), eq(adminId));

            mockMvc.perform(patch("/api/categories/{id}/activate", categoryId)
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("C013"));
        }
    }

    @Nested
    @DisplayName("인증 및 권한 테스트")
    class AuthenticationTests {

        @ParameterizedTest
        @ValueSource(strings = {"", "invalid-token", "Bearer", "Bearer ", "Basic token"})
        @DisplayName("실패 - 잘못된 Authorization 헤더 형식")
        void failWithInvalidAuthHeader(String authHeader) throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("아무거나")
                    .build();
            given(jwtUtil.extractTokenFromHeader(authHeader)).willReturn(null);

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", authHeader)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(categoryService, never()).createCategory(any(), any());
        }

        @Test
        @DisplayName("실패 - 만료된 토큰")
        void failWithExpiredToken() throws Exception {
            
            String expiredToken = "expired-token";
            given(jwtUtil.extractTokenFromHeader("Bearer " + expiredToken))
                    .willReturn(expiredToken);
            given(jwtUtil.isTokenValid(expiredToken))
                    .willReturn(false);
            CategoryRequest request = CategoryRequest.builder()
                    .name("아무거나")
                    .build();
            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + expiredToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("A003"));
        }
    }

    @Nested
    @DisplayName("에러 핸들링 테스트")
    class ErrorHandlingTests {

        @Test
        @DisplayName("실패 - 서버 내부 오류")
        void handleInternalServerError() throws Exception {
            
            given(categoryService.getAllCategories(any()))
                    .willThrow(new RuntimeException("Database connection failed"));

            
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("실패 - 잘못된 JSON 형식")
        void handleMalformedJson() throws Exception {
            
            String jsonWithEmptyName = "{\"name\": \"\", \"description\": \"설명\"}";

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonWithEmptyName))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(any(), any());
        }

        @Test
        @DisplayName("실패 - Content-Type 누락")
        void handleMissingContentType() throws Exception {
            
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + validToken)
                            .content("{}"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    private CategoryResponse createSampleResponse(Long id, String name, String description,
                                                  Long parentId, int depth, Gender gender, int displayOrder) {
        return CategoryResponse.builder()
                .id(id)
                .name(name)
                .description(description)
                .parentId(parentId)
                .depth(depth)
                .path(parentId == null ? "/" + id : "/1/" + id)
                .gender(gender)
                .displayOrder(displayOrder)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .createdBy(adminId)
                .updatedBy(adminId)
                .children(new ArrayList<>()) // 변경 가능한 리스트로 수정!
                .build();
    }
}