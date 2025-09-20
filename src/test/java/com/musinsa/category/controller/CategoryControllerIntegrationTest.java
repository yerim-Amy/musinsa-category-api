package com.musinsa.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.entity.Category;
import com.musinsa.category.enums.Gender;
import com.musinsa.category.exception.ErrorCode;
import com.musinsa.category.repository.CategoryRepository;
import com.musinsa.category.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CategoryController 통합 테스트")
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private JwtUtil jwtUtil;

    private String validToken;
    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid.jwt.token";
        given(jwtUtil.extractTokenFromHeader(validToken)).willReturn("valid.jwt.token");
        given(jwtUtil.isTokenValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractAdminId("valid.jwt.token")).willReturn("admin");
        given(jwtUtil.getExpirationDate("valid.jwt.token")).willReturn(new Date());

        // 테스트 데이터 생성
        setupTestData();
    }

    private void setupTestData() {
        // 기존 데이터 정리
        categoryRepository.deleteAll();

        // 부모 카테고리 생성
        parentCategory = Category.builder()
                .name("상의")
                .description("상의 카테고리")
                .gender(Gender.A)
                .depth(1)
                .displayOrder(1)
                .path("/상의")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedAt(LocalDateTime.now())
                .updatedBy("admin")
                .build();
        parentCategory = categoryRepository.save(parentCategory);

        // 자식 카테고리 생성
        childCategory = Category.builder()
                .name("티셔츠")
                .description("티셔츠 카테고리")
                .parent(parentCategory)
                .gender(Gender.M)
                .depth(2)
                .displayOrder(1)
                .path("/상의/티셔츠")
                .isActive(true)
                .createdBy("admin")
                .updatedBy("admin")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        childCategory = categoryRepository.save(childCategory);
    }

    @Nested
    @DisplayName("카테고리 생성 통합테스트")
    class CreateCategoryIntegration {

        @Test
        @DisplayName("카테고리 생성 - 전체 플로우 성공")
        void createCategory_Success() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("새로운 카테고리")
                    .description("새로운 카테고리 설명")
                    .gender(Gender.F)
                    .displayOrder(2)
                    .build();

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.name", is("새로운 카테고리")))
                    .andExpect(jsonPath("$.data.description", is("새로운 카테고리 설명")))
                    .andExpect(jsonPath("$.data.gender", is("F")))
                    .andExpect(jsonPath("$.data.depth", is(0)))
                    .andExpect(jsonPath("$.data.displayOrder", is(2)))
                    .andExpect(jsonPath("$.data.parentId").doesNotExist())
                    .andExpect(jsonPath("$.data.id", notNullValue()));

            // 데이터베이스 검증
            Category created = categoryRepository.findAll().stream()
                    .filter(c -> "새로운 카테고리".equals(c.getName()))
                    .findFirst()
                    .orElse(null);

            assertThat(created).isNotNull();
            assertThat(created.getGender()).isEqualTo(Gender.F);
            assertThat(created.getCreatedBy()).isEqualTo("admin");
        }

        @Test
        @DisplayName("자식 카테고리 생성 - 부모 관계 설정 성공")
        void createCategory_WithParentsAndChild_Return201() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("맨투맨")
                    .description("맨투맨 카테고리")
                    .parentId(parentCategory.getId())
                    .gender(Gender.A)
                    .displayOrder(2)
                    .build();
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name", is("맨투맨")))
                    .andExpect(jsonPath("$.data.parentId", is(parentCategory.getId().intValue())))
                    .andExpect(jsonPath("$.data.depth", is(2)))
                    .andExpect(jsonPath("$.data.path", startsWith("/상의/")));

            Category created = categoryRepository.findAll().stream()
                    .filter(c -> "맨투맨".equals(c.getName()))
                    .findFirst()
                    .orElse(null);

            assertThat(created.getParent().getId()).isEqualTo(parentCategory.getId());
        }

        @Test
        @DisplayName("카테고리 생성 실패 - 이름 중복")
        void createCategory_DuplicateName_Returns404() throws Exception {
            // 이미 존재하는 이름으로 요청
            CategoryRequest request = CategoryRequest.builder()
                    .name("상의")  // 이미 존재하는 이름
                    .description("중복 테스트")
                    .gender(Gender.A)
                    .displayOrder(3)
                    .build();

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.code", is(ErrorCode.CATEGORY_NAME_DUPLICATE.getCode())))
                    .andExpect(jsonPath("$.message", is(ErrorCode.CATEGORY_NAME_DUPLICATE.getMessage())));
        }

        @Test
        @DisplayName("카테고리 생성 실패 - 존재하지 않는 부모")
        void createCategory_NonExistentParent_Returns404() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트 카테고리")
                    .parentId(999L)  // 존재하지 않는 부모 ID
                    .gender(Gender.A)
                    .build();

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(ErrorCode.CATEGORY_PARENT_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.message", containsString(ErrorCode.CATEGORY_PARENT_NOT_FOUND.getMessage())));
        }

        @Test
        @DisplayName("카테고리 생성 실패 - 인증 토큰 없음")
        void createCategory_NoToken_Returns401() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트")
                    .build();

            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("카테고리 수정 통합테스트")
    class UpdateCategoryIntegration {

        @Test
        @DisplayName("카테고리 수정 - 전체 플로우 성공")
        void updateCategory_Success() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("수정된 상의")
                    .description("수정된 설명")
                    .gender(Gender.F)
                    .displayOrder(5)
                    .build();

            mockMvc.perform(put("/api/categories/{id}", parentCategory.getId())
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("수정된 상의")))
                    .andExpect(jsonPath("$.data.description", is("수정된 설명")))
                    .andExpect(jsonPath("$.data.gender", is("F")))
                    .andExpect(jsonPath("$.data.displayOrder", is(5)));

            // 데이터베이스 검증
            Category updated = categoryRepository.findById(parentCategory.getId()).orElse(null);
            assertThat(updated.getName()).isEqualTo("수정된 상의");
            assertThat(updated.getUpdatedBy()).isEqualTo("admin");
        }

        @Test
        @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
        void updateCategory_NonExistentCategory_Returns404() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트")
                    .build();

            mockMvc.perform(put("/api/categories/{id}", 999L)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code", is(ErrorCode.CATEGORY_NOT_FOUND.getCode())));
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 통합테스트")
    class DeleteCategoryIntegration {

        @Test
        @DisplayName("카테고리 삭제 - 자식 카테고리 삭제 후, 부모 삭제")
        void deleteCategory_DeleteChildFirst_Success() throws Exception {
            // 자식 카테고리 삭제
            mockMvc.perform(delete("/api/categories/{id}", childCategory.getId())
                            .header("Authorization", validToken))
                    .andExpect(status().isOk());

            // 부모 카테고리 삭제
            mockMvc.perform(delete("/api/categories/{id}", parentCategory.getId())
                            .header("Authorization", validToken))
                    .andExpect(status().isOk());

            // 3. 데이터베이스 검증 - 비활성화되었는지 확인
            Category deletedChild = categoryRepository.findById(childCategory.getId()).orElse(null);
            Category deletedParent = categoryRepository.findById(parentCategory.getId()).orElse(null);

            assertThat(deletedChild.getIsActive()).isFalse();
            assertThat(deletedParent.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("카테고리 삭제 실패 - 하위 카테고리 존재")
        void deleteCategory_HasChildren_Returns400() throws Exception {
            // 하위가 있는 부모 삭제 시도
            mockMvc.perform(delete("/api/categories/{id}", parentCategory.getId())
                            .header("Authorization", validToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(ErrorCode.CATEGORY_HAS_CHILDREN.getCode())))
                    .andExpect(jsonPath("$.message", is(ErrorCode.CATEGORY_HAS_CHILDREN.getMessage())));
        }
    }

    @Nested
    @DisplayName("카테고리 조회 통합테스트")
    class GetCategoryIntegration {

        @Test
        @DisplayName("단일 카테고리 조회 - 성공")
        void getCategory_ExistingId_ReturnsCategory() throws Exception {
            mockMvc.perform(get("/api/categories/{id}", parentCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(parentCategory.getId().intValue())))
                    .andExpect(jsonPath("$.data.name", is("상의")))
                    .andExpect(jsonPath("$.data.description", is("상의 카테고리")));
        }

        @Test
        @DisplayName("카테고리 목록 조회 - 성별 필터링")
        void getCategories_WithGender_ReturnsFilteredList() throws Exception {
            // 전체 성별로 조회
            mockMvc.perform(get("/api/categories")
                            .param("gender", "A"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.data[0].name", notNullValue()));

            // 남성 카테고리만 조회
            mockMvc.perform(get("/api/categories")
                            .param("gender", "M"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("루트 카테고리들 조회")
        void getRootCategories_ValidGender_ReturnsRootList() throws Exception {
            mockMvc.perform(get("/api/categories/roots")
                            .param("gender", "A"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.data[0].depth", is(1)))
                    .andExpect(jsonPath("$.data[0].parentId").doesNotExist());
        }

        @Test
        @DisplayName("카테고리 트리 조회")
        void getCategoryTree_ValidGender_ReturnsTreeStructure() throws Exception {
            mockMvc.perform(get("/api/categories/tree")
                            .param("gender", "A"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.data[0].children", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("하위 카테고리들 조회")
        void getDirectChildren_ExistingParent_ReturnsChildren() throws Exception {
            mockMvc.perform(get("/api/categories/{id}/children", parentCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].name", is("티셔츠")))
                    .andExpect(jsonPath("$.data[0].parentId", is(parentCategory.getId().intValue())));
        }

        @Test
        @DisplayName("카테고리 검색")
        void searchCategories_ValidKeyword_ReturnsResults() throws Exception {
            mockMvc.perform(get("/api/categories/search")
                            .param("keyword", "상의"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.data[0].name", containsString("상의")));
        }

        @Test
        @DisplayName("카테고리 검색 실패 - 키워드 길이 부족")
        void searchCategories_ShortKeyword_Returns400() throws Exception {
            mockMvc.perform(get("/api/categories/search")
                            .param("keyword", "a"))  // 1글자
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(ErrorCode.INVALID_INPUT_VALUE.getCode())));
        }
    }

    @Nested
    @DisplayName("카테고리 활성화 통합테스트")
    class ActivateCategoryIntegration {

        @Test
        @DisplayName("비활성 카테고리 활성화 - 성공")
        void activateCategory_InactiveCategory_ReturnsSuccess() throws Exception {
            // 카테고리를 먼저 비활성화
            childCategory.deactivate();
            categoryRepository.save(childCategory);

            // 활성화 요청
            mockMvc.perform(patch("/api/categories/{id}/activate", childCategory.getId())
                            .header("Authorization", validToken))
                    .andExpect(status().isOk());

            // 데이터베이스 검증
            Category activated = categoryRepository.findById(childCategory.getId()).orElse(null);
            assertThat(activated.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("카테고리 활성화 실패 - 이미 활성상태")
        void activateCategory_AlreadyActive_Returns400() throws Exception {
            // 이미 활성화된 카테고리 활성화 시도
            mockMvc.perform(patch("/api/categories/{id}/activate", parentCategory.getId())
                            .header("Authorization", validToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(ErrorCode.CATEGORY_ALREADY_ACTIVE.getCode())))
                    .andExpect(jsonPath("$.message", is(ErrorCode.CATEGORY_ALREADY_ACTIVE.getMessage())));
        }
    }

    @Nested
    @DisplayName("데이터 검증 통합테스트")
    class DataValidationIntegration {

        @Test
        @DisplayName("displayOrder 중복 검증")
        void createCategory_WithDuplicateDisplayOrder_Returns400() throws Exception {
            // 이미 사용중인 displayOrder로 카테고리 생성 시도
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트 카테고리")
                    .displayOrder(1)  // parentCategory와 동일한 displayOrder
                    .gender(Gender.A)
                    .build();

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(ErrorCode.DISPLAY_ORDER_DUPLICATE.getCode())));
        }

        @Test
        @DisplayName("카테고리명 유효성 검증 - 빈 이름")
        void createCategory_EmptyName_Returns400() throws Exception {
            CategoryRequest request = CategoryRequest.builder()
                    .name(" ")  // 빈 이름
                    .gender(Gender.A)
                    .build();

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(ErrorCode.INVALID_INPUT_VALUE.getCode())));
        }

        @Test
        @DisplayName("카테고리명 길이 검증 - 너무 긴 이름")
        void createCategory_TooLongName_Returns400() throws Exception {
            // 100자가 넘는 이름
            String longName = "a".repeat(101);
            CategoryRequest request = CategoryRequest.builder()
                    .name(longName)
                    .gender(Gender.A)
                    .build();

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(ErrorCode.CATEGORY_NAME_TOO_LONG.getCode())));
        }
    }
}