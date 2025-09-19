package com.musinsa.category.repository;

import com.musinsa.category.entity.Category;
import com.musinsa.category.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CategoryRepositoryTest 단위 테스트")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category rootCategory;
    private Category childCategory;
    private Category inactiveCategory;
    private Category maleCategory;
    private Category femaleCategory;

    @BeforeEach
    void setUp() {
        rootCategory = createCategory("루트카테고리", null, 1, Gender.A, true, "/1", 0);
        entityManager.persistAndFlush(rootCategory);

        childCategory = createCategory("자식카테고리", rootCategory, 1, Gender.A, true, "/1/2", 1);
        entityManager.persistAndFlush(childCategory);

        inactiveCategory = createCategory("비활성카테고리", null, 2, Gender.A, false, "/3", 0);
        entityManager.persistAndFlush(inactiveCategory);

        maleCategory = createCategory("남성카테고리", null, 3, Gender.M, true, "/4", 0);
        entityManager.persistAndFlush(maleCategory);

        femaleCategory = createCategory("여성카테고리", null, 4, Gender.F, true, "/5", 0);
        entityManager.persistAndFlush(femaleCategory);
    }

    @Test
    @DisplayName("ID로 활성화된 카테고리 조회 - 성공")
    void findActiveById_Success() {
        Long categoryId = rootCategory.getId();
        Optional<Category> result = categoryRepository.findActiveById(categoryId);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("루트카테고리");
        assertThat(result.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("ID로 활성화된 카테고리 조회 - 비활성 카테고리는 조회되지 않음")
    void findActiveById_InactiveNotFound() {
        Long inactiveCategoryId = inactiveCategory.getId();
        Optional<Category> result = categoryRepository.findActiveById(inactiveCategoryId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("모든 활성화된 카테고리 조회 - ALL 성별")
    void findAllActiveOrdered_AllGender() {
        List<Category> result = categoryRepository.findAllActiveOrdered(Gender.A.name());
        assertThat(result).hasSize(4); // 활성 카테고리만 조회
        assertThat(result).extracting("name")
                .containsExactly("루트카테고리",  "남성카테고리", "여성카테고리","자식카테고리");
    }

    @Test
    @DisplayName("모든 활성화된 카테고리 조회 - 남성 성별")
    void findAllActiveOrdered_MaleGender() {
        List<Category> result = categoryRepository.findAllActiveOrdered("M");
        assertThat(result).hasSize(3); // A, M 성별 카테고리만 조회
        assertThat(result).extracting("name")
                .containsExactly("루트카테고리", "남성카테고리", "자식카테고리"); // depth, displayOrder 순으로 정렬
    }


    @Test
    @DisplayName("루트 카테고리들 조회 - ALL 성별 (모든 성별 조회)")
    void findRootCategories_AllGender() {
        List<Category> allCategories = categoryRepository.findAll();
        System.out.println("=== 저장된 모든 카테고리 ===");
        allCategories.forEach(c ->
                System.out.println(String.format("ID: %d, Name: %s, Gender: %s, Parent: %s",
                        c.getId(), c.getName(), c.getGender(),
                        c.getParent() != null ? c.getParent().getName() : "null"))
        );

        List<Category> result = categoryRepository.findRootCategories(Gender.A.name());
        System.out.println("=== 조회 결과 ===");
        result.forEach(c ->
                System.out.println(String.format("ID: %d, Name: %s, Gender: %s",
                        c.getId(), c.getName(), c.getGender()))
        );
        assertThat(result).hasSize(3); // 모든 성별의 루트 카테고리 조회
        assertThat(result).extracting("name")
                .containsExactly("루트카테고리", "남성카테고리", "여성카테고리");
        assertThat(result).allMatch(category -> category.getParent() == null);
    }

    @Test
    @DisplayName("루트 카테고리들 조회 - 남성 성별")
    void findRootCategories_MaleGender() {
        List<Category> result = categoryRepository.findRootCategories(Gender.M.name());
        assertThat(result).hasSize(2); // Gender.A, Gender.M 조회됨
        assertThat(result).extracting("name")
                .containsExactly("루트카테고리", "남성카테고리");
        assertThat(result).allMatch(category -> category.getParent() == null);
    }

    @Test
    @DisplayName("루트 카테고리들 조회 - 여성 성별")
    void findRootCategories_FemaleGender() {
        List<Category> result = categoryRepository.findRootCategories(Gender.F.name());
        assertThat(result).hasSize(2); // Gender.A, Gender.F 조회됨
        assertThat(result).extracting("name")
                .containsExactly("루트카테고리", "여성카테고리");
        assertThat(result).allMatch(category -> category.getParent() == null);
    }

    @Test
    @DisplayName("부모 정보 포함한 모든 활성 카테고리 조회")
    void findAllActiveWithParent() {
        List<Category> result = categoryRepository.findAllActiveWithParent(Gender.A.name());
        assertThat(result).hasSize(4);

        // 자식 카테고리의 부모 정보가 로드되었는지 확인
        Category foundChildCategory = result.stream()
                .filter(c -> c.getName().equals("자식카테고리"))
                .findFirst()
                .orElse(null);

        assertThat(foundChildCategory).isNotNull();
        assertThat(foundChildCategory.getParent()).isNotNull();
        assertThat(foundChildCategory.getParent().getName()).isEqualTo("루트카테고리");
    }

    @Test
    @DisplayName("특정 부모의 자식 카테고리들 조회")
    void findChildrenByParentId() {
        List<Category> result = categoryRepository.findChildrenByParentId(rootCategory.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("자식카테고리");
        assertThat(result.get(0).getParent().getId()).isEqualTo(rootCategory.getId());
    }

    @Test
    @DisplayName("하위 카테고리들 조회")
    void findDescendants() {
        String parentPath = "/1";
        List<Category> result = categoryRepository.findDescendants(parentPath, Gender.A.name());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("자식카테고리");
        assertThat(result.get(0).getPath()).startsWith(parentPath + "/");
    }

    @Test
    @DisplayName("이름으로 카테고리 검색")
    void searchByName() {
        List<Category> result = categoryRepository.searchByName("루트");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("루트카테고리");
    }

    @Test
    @DisplayName("같은 부모 하위에서 이름 중복 확인 - 중복 있음")
    void existsByNameAndParent_Exists() {
        boolean exists = categoryRepository.existsByNameAndParent("자식카테고리", rootCategory.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("같은 부모 하위에서 이름 중복 확인 - 중복 없음")
    void existsByNameAndParent_NotExists() {
        boolean exists = categoryRepository.existsByNameAndParent("존재하지않는카테고리", rootCategory.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("루트 레벨에서 이름 중복 확인")
    void existsByNameAndParent_RootLevel() {
        boolean exists = categoryRepository.existsByNameAndParent("루트카테고리", null);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("부모 ID와 정렬 순서로 카테고리 찾기")
    void findByParentIdAndDisplayOrder() {
        Optional<Category> result = categoryRepository.findByParentIdAndDisplayOrder(rootCategory.getId(), 1);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("자식카테고리");
    }

    @Test
    @DisplayName("루트 레벨에서 정렬 순서로 카테고리 찾기")
    void findByParentIdAndDisplayOrder_RootLevel() {
        Optional<Category> result = categoryRepository.findByParentIdAndDisplayOrder(null, 1);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("루트카테고리");
    }

    @Test
    @DisplayName("존재하지 않는 정렬 순서로 조회시 빈 결과")
    void findByParentIdAndDisplayOrder_NotFound() {
        Optional<Category> result = categoryRepository.findByParentIdAndDisplayOrder(rootCategory.getId(), 999);
        assertThat(result).isEmpty();
    }

    /**
     * 테스트용 Category 엔티티 생성 헬퍼 메서드
     */
    private Category createCategory(String name, Category parent, Integer displayOrder,
                                    Gender gender, Boolean isActive, String path, Integer depth) {
        return Category.builder()
                .name(name)
                .description(name + " 설명")
                .parent(parent)
                .displayOrder(displayOrder)
                .gender(gender)
                .isActive(isActive)
                .path(path)
                .depth(depth)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("testUser")
                .updatedBy("testUser")
                .build();
    }
}