package com.musinsa.category.service;

import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.dto.CategoryResponse;
import com.musinsa.category.entity.Category;
import com.musinsa.category.enums.Gender;
import com.musinsa.category.exception.BusinessException;
import com.musinsa.category.exception.ErrorCode;
import com.musinsa.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 단위 테스트")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category parentCategory;
    private Category childCategory;
    private CategoryRequest validRequest;

    @BeforeEach
    void setUp() {
        parentCategory = createCategory(1L, "상의", null, 0, "/1");
        childCategory = createCategory(2L, "티셔츠", parentCategory, 1, "/1/2");
        validRequest = createValidRequest();
    }

    @Nested
    @DisplayName("카테고리 생성")
    class CreateCategory {

        @Test
        @DisplayName("성공 - 루트 카테고리 생성")
        void createRootCategory_Success() {
            CategoryRequest request = CategoryRequest.builder()
                    .name("상의")
                    .description("상의 카테고리")
                    .gender(Gender.A)
                    .displayOrder(1)
                    .build();

            given(categoryRepository.existsByNameAndParent(anyString(), isNull()))
                    .willReturn(false);
            given(categoryRepository.findByParentIdAndDisplayOrder(isNull(), eq(1)))
                    .willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class)))
                    .willAnswer(invocation -> {
                        Category category = invocation.getArgument(0);
                        return createCategoryWithId(category, 1L);
                    });

            CategoryResponse response = categoryService.createCategory(request, "admin");

            assertThat(response.getName()).isEqualTo("상의");
            assertThat(response.getDescription()).isEqualTo("상의 카테고리");
            assertThat(response.getGender()).isEqualTo(Gender.A);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("성공 - 자식 카테고리 생성")
        void createChildCategory_Success() {
            CategoryRequest request = CategoryRequest.builder()
                    .name("티셔츠")
                    .parentId(1L)
                    .gender(Gender.A)
                    .displayOrder(1)
                    .build();

            given(categoryRepository.existsByNameAndParent("티셔츠", 1L))
                    .willReturn(false);
            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByParentIdAndDisplayOrder(1L, 1))
                    .willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class)))
                    .willAnswer(invocation -> createCategoryWithId(invocation.getArgument(0), 2L));

            CategoryResponse response = categoryService.createCategory(request, "admin");

            assertThat(response.getName()).isEqualTo("티셔츠");
            assertThat(response.getParentId()).isEqualTo(1L);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("실패 - 카테고리명 필수")
        void createCategory_FailWhenNameIsEmpty() {
            
            CategoryRequest request = CategoryRequest.builder()
                    .name("")
                    .build();

            assertThatThrownBy(() -> categoryService.createCategory(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NAME_REQUIRED);
        }

        @Test
        @DisplayName("실패 - 카테고리명 중복")
        void createCategory_FailWhenNameDuplicated() {
            
            CategoryRequest request = createValidRequest();
            given(categoryRepository.existsByNameAndParent("테스트카테고리", null))
                    .willReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        @Test
        @DisplayName("실패 - 부모 카테고리 존재하지 않음")
        void createCategory_FailWhenParentNotFound() {
            
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트")
                    .parentId(999L)
                    .build();

            given(categoryRepository.existsByNameAndParent("테스트", 999L))
                    .willReturn(false);
            given(categoryRepository.findActiveById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.createCategory(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_PARENT_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 최대 깊이 초과")
        void createCategory_FailWhenDepthExceeded() {
            
            Category deepParent = createCategory(1L, "깊은부모", null, 4, "/1");
            CategoryRequest request = CategoryRequest.builder()
                    .name("깊은자식")
                    .parentId(1L)
                    .build();

            given(categoryRepository.existsByNameAndParent("깊은자식", 1L))
                    .willReturn(false);
            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(deepParent));

            assertThatThrownBy(() -> categoryService.createCategory(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_DEPTH_EXCEEDED);
        }

        @Test
        @DisplayName("실패 - displayOrder 중복")
        void createCategory_FailWhenDisplayOrderDuplicated() {
            
            CategoryRequest request = CategoryRequest.builder()
                    .name("테스트")
                    .displayOrder(1)
                    .build();

            given(categoryRepository.existsByNameAndParent("테스트", null))
                    .willReturn(false);
            given(categoryRepository.findByParentIdAndDisplayOrder(null, 1))
                    .willReturn(Optional.of(parentCategory));

            assertThatThrownBy(() -> categoryService.createCategory(request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DISPLAY_ORDER_DUPLICATE);
        }
    }

    @Nested
    @DisplayName("카테고리 수정")
    class UpdateCategory {

        @Test
        @DisplayName("성공 - 카테고리 정보 수정")
        void updateCategory_Success() {
            
            CategoryRequest request = CategoryRequest.builder()
                    .name("수정된이름")
                    .description("수정된설명")
                    .displayOrder(2)
                    .gender(Gender.M)
                    .build();

            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(parentCategory));
            given(categoryRepository.existsByNameAndParent("수정된이름", null))
                    .willReturn(false);
            given(categoryRepository.findByParentIdAndDisplayOrder(null, 2))
                    .willReturn(Optional.empty());

            CategoryResponse response = categoryService.updateCategory(1L, request, "admin");

            assertThat(response.getName()).isEqualTo("수정된이름");
            verify(categoryRepository).findActiveById(1L);
            verify(categoryRepository).existsByNameAndParent("수정된이름", null);
            verify(categoryRepository).findByParentIdAndDisplayOrder(null, 2);
        }

        @Test
        @DisplayName("실패 - 카테고리 존재하지 않음")
        void updateCategory_FailWhenCategoryNotFound() {
            given(categoryRepository.findActiveById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(999L, validRequest, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 자기 자신을 부모로 설정")
        void updateCategory_FailWhenSelfParent() {
            CategoryRequest request = CategoryRequest.builder()
                    .parentId(1L)
                    .build();

            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(parentCategory));

            assertThatThrownBy(() -> categoryService.updateCategory(1L, request, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_SELF_PARENT);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteCategory {

        @Test
        @DisplayName("성공 - 카테고리 비활성화")
        void deleteCategory_Success() {
            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(parentCategory));
            given(categoryRepository.findChildrenByParentId(1L))
                    .willReturn(Collections.emptyList());  // 하위 카테고리 없음

            assertThatCode(() -> categoryService.deleteCategory(1L, "admin"))
                    .doesNotThrowAnyException();

            // Repository 호출 검증
            verify(categoryRepository).findActiveById(1L);
            verify(categoryRepository).findChildrenByParentId(1L);
        }

        @Test
        @DisplayName("실패 - 하위 카테고리 존재")
        void deleteCategory_FailWhenHasChildren() {
            
            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(parentCategory));
            given(categoryRepository.findChildrenByParentId(1L))
                    .willReturn(Arrays.asList(childCategory));

            assertThatThrownBy(() -> categoryService.deleteCategory(1L, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_HAS_CHILDREN);
        }
    }

    @Nested
    @DisplayName("카테고리 조회")
    class GetCategory {

        @Test
        @DisplayName("성공 - 단일 카테고리 조회")
        void getCategoryById_Success() {
            
            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(parentCategory));

            CategoryResponse response = categoryService.getCategoryById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("상의");
        }

        @Test
        @DisplayName("성공 - 하위 카테고리들 조회")
        void getDirectChildren_Success() {
            given(categoryRepository.findActiveById(1L))
                    .willReturn(Optional.of(parentCategory));
            given(categoryRepository.findChildrenByParentId(1L))
                    .willReturn(Arrays.asList(childCategory));

            List<CategoryResponse> children = categoryService.getDirectChildren(1L);

            assertThat(children).hasSize(1);
            assertThat(children.get(0).getName()).isEqualTo("티셔츠");
        }

        @Test
        @DisplayName("성공 - 루트 카테고리들 조회")
        void getRootCategories_Success() {
            given(categoryRepository.findRootCategories(Gender.A))
                    .willReturn(Arrays.asList(parentCategory));

            List<CategoryResponse> roots = categoryService.getRootCategories(Gender.A);

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getName()).isEqualTo("상의");
        }

        @Test
        @DisplayName("성공 - 카테고리 트리 조회")
        void getCategoryTree_Success() {
            
            given(categoryRepository.findAllActiveWithParent(Gender.A))
                    .willReturn(Arrays.asList(parentCategory, childCategory));

            List<CategoryResponse> tree = categoryService.getCategoryTree(null, Gender.A);

            assertThat(tree).hasSize(1);
            assertThat(tree.get(0).getName()).isEqualTo("상의");
            assertThat(tree.get(0).getChildren()).hasSize(1);
            assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("티셔츠");
        }

        @Test
        @DisplayName("성공 - 카테고리 검색")
        void searchCategories_Success() {
            
            String keyword = "상의";
            given(categoryRepository.searchByName(keyword))
                    .willReturn(Arrays.asList(parentCategory));

            List<CategoryResponse> results = categoryService.searchCategories(keyword);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("상의");
        }

        @Test
        @DisplayName("실패 - 검색 키워드 길이 부족")
        void searchCategories_FailWhenKeywordTooShort() {
            // When & Then - 예외 발생 기대
            assertThatThrownBy(() -> categoryService.searchCategories("a"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // Helper methods
    private Category createCategory(Long id, String name, Category parent, int depth, String path) {
        Category category = Category.builder()
                .name(name)
                .parent(parent)
                .depth(depth)
                .path(path)
                .displayOrder(1)
                .gender(Gender.A)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            java.lang.reflect.Field idField = Category.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(category, id);
        } catch (Exception e) {
        }

        return category;
    }

    private CategoryRequest createValidRequest() {
        return CategoryRequest.builder()
                .name("테스트카테고리")
                .description("테스트 설명")
                .gender(Gender.A)
                .displayOrder(1)
                .build();
    }

    private Category createCategoryWithId(Category category, Long id) {
        try {
            java.lang.reflect.Field idField = Category.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(category, id);
        } catch (Exception e) {
        }
        return category;
    }
}