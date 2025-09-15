package com.musinsa.category.service;

import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.dto.CategoryResponse;
import com.musinsa.category.dto.CategoryStatsResponse;
import com.musinsa.category.entity.Category;
import com.musinsa.category.exception.BusinessException;
import com.musinsa.category.exception.ErrorCode;
import com.musinsa.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private static final int MAX_CATEGORY_DEPTH = 4;
    private static final int MAX_CATEGORY_NAME_LENGTH = 100;

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, String adminId) {
        log.info("카테고리 생성 요청 - name: {}, parentId: {}, adminId: {}",
                request.getName(), request.getParentId(), adminId);

        // 입력값 검증
        validateCategoryRequest(request);

        // 중복 이름 검증
        checkDuplicateName(request.getName(), request.getParentId());

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findActiveById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_PARENT_NOT_FOUND));

            // 깊이 제한 검증
            if (parent.getDepth() >= MAX_CATEGORY_DEPTH) {
                throw new BusinessException(ErrorCode.CATEGORY_DEPTH_EXCEEDED);
            }
        }

        // 다음 정렬 순서 계산
        Integer nextOrder = getNextDisplayOrder(parent);

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .parent(parent)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : nextOrder)
                .isActive(true)
                .createdBy(adminId)
                .updatedBy(adminId)
                .build();

        Category savedCategory = categoryRepository.save(category);
        savedCategory.updatePathAndDepth();

        log.info("카테고리 생성 완료 - ID: {}, Path: {}, adminId: {}",
                savedCategory.getId(), savedCategory.getPath(), adminId);
        return CategoryResponse.from(savedCategory);
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, String adminId) {
        log.info("카테고리 수정 요청 - ID: {}, adminId: {}", categoryId, adminId);

        Category category = categoryRepository.findActiveById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 이름 변경 시 중복 검증
        if (StringUtils.hasText(request.getName()) && !request.getName().equals(category.getName())) {
            Long parentId = category.getParent() != null ? category.getParent().getId() : null;
            checkDuplicateNameForUpdate(request.getName(), parentId, categoryId);
        }

        category.updateInfo(request.getName(), request.getDescription(), request.getDisplayOrder());
        category.updateAuditInfo(adminId);

        log.info("카테고리 수정 완료 - ID: {}, adminId: {}", categoryId, adminId);
        return CategoryResponse.from(category);
    }

    /**
     * 카테고리 삭제 (비활성화)
     */
    @Transactional
    public void deleteCategory(Long categoryId, String adminId) {
        log.info("카테고리 삭제 요청 - ID: {}, adminId: {}", categoryId, adminId);

        Category category = categoryRepository.findActiveById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 하위 카테고리 존재 확인
        List<Category> activeChildren = categoryRepository.findChildrenByParentId(categoryId);
        if (!activeChildren.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        // 비활성화 처리
        category.deactivate();
        category.updateAuditInfo(adminId);

        log.info("카테고리 삭제 완료 - ID: {}, adminId: {}", categoryId, adminId);
    }

    /**
     * 카테고리 활성화
     */
    @Transactional
    public void activateCategory(Long categoryId, String adminId) {
        log.info("카테고리 활성화 요청 - ID: {}, adminId: {}", categoryId, adminId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 부모가 비활성 상태면 활성화 불가
        if (category.getParent() != null && !category.getParent().getIsActive()) {
            throw new BusinessException(ErrorCode.CATEGORY_INACTIVE_PARENT);
        }

        category.activate();
        category.updateAuditInfo(adminId);

        log.info("카테고리 활성화 완료 - ID: {}, adminId: {}", categoryId, adminId);
    }

    // ==== 조회 API ====
    /**
     * 카테고리 단일 조회
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        log.debug("카테고리 조회 - ID: {}", categoryId);

        Category category = categoryRepository.findActiveById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        return CategoryResponse.from(category);
    }

    /**
     * 특정 카테고리의 직계 하위 카테고리 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getDirectChildren(Long parentId) {
        log.debug("직계 하위 카테고리 조회 - 부모 ID: {}", parentId);

        List<Category> children;
        if (parentId == null) {
            // 루트 카테고리들 조회
            children = categoryRepository.findRootCategories();
        } else {
            // 부모 존재 여부 확인
            categoryRepository.findActiveById(parentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

            // 하위 카테고리들 조회
            children = categoryRepository.findChildrenByParentId(parentId);
        }

        return children.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 트리 구조 조회 (전체 또는 특정 카테고리 기준)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryTree", key = "#categoryId != null ? #categoryId : 'all'")
    public List<CategoryResponse> getCategoryTree(Long categoryId) {
        log.debug("카테고리 트리 조회 - ID: {}", categoryId);

        List<Category> categories;

        if (categoryId == null) {
            // 전체 카테고리 트리 조회
            categories = categoryRepository.findAllActiveOrdered();
        } else {
            // 특정 카테고리와 하위 카테고리들 조회
            Category rootCategory = categoryRepository.findActiveById(categoryId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

            categories = categoryRepository.findDescendants(rootCategory.getPath());
            categories.add(0, rootCategory); // 본인도 포함
        }

        return buildTreeStructure(categories);
    }

    /**
     * 루트 카테고리들 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.debug("루트 카테고리 조회");

        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 전체 카테고리 조회 (활성화된 것만)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("전체 카테고리 조회");

        List<Category> categories = categoryRepository.findAllActiveOrdered();
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 검색 (이름 기반)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String keyword) {
        log.debug("카테고리 검색 - keyword: {}", keyword);

        if (!StringUtils.hasText(keyword) || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        List<Category> categories = categoryRepository.searchByName(keyword.trim());

        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 통계 조회
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryStats")
    public CategoryStatsResponse getCategoryStats() {
        log.debug("카테고리 통계 조회");

        long totalCount = categoryRepository.countAllActive();        // 전체 카테고리 개수
        long rootCount = categoryRepository.countRootCategories();    // 대분류 개수
        long leafCount = categoryRepository.countLeafCategories();    // 실제 상품 들어갈 카테고리 개수
        int maxDepth = categoryRepository.findMaxDepth();             // 카테고리 깊이

        return CategoryStatsResponse.of(totalCount, rootCount, leafCount, maxDepth);
    }

    // ==== Private Helper Methods ====

    private void validateCategoryRequest(CategoryRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_REQUIRED);
        }

        String trimmedName = request.getName().trim();
        if (trimmedName.length() > MAX_CATEGORY_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_TOO_LONG);
        }
    }

    private void checkDuplicateName(String name, Long parentId) {
        boolean exists = categoryRepository.existsByNameAndParent(name.trim(), parentId);
        if (exists) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    private void checkDuplicateNameForUpdate(String name, Long parentId, Long excludeId) {
        List<Category> existingCategories = categoryRepository.findByNameAndParent(name.trim(), parentId);
        boolean hasDuplicate = existingCategories.stream()
                .anyMatch(category -> !category.getId().equals(excludeId));

        if (hasDuplicate) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    private Integer getNextDisplayOrder(Category parent) {
        if (parent == null) {
            // 루트 레벨의 다음 순서
            List<Category> siblings = categoryRepository.findRootCategoriesDesc();
            if (siblings.isEmpty()) {
                return 1;
            }
            return siblings.get(0).getDisplayOrder() + 1;
        } else {
            // 특정 부모의 하위 레벨 다음 순서
            List<Category> siblings = categoryRepository.findChildrenByParentIdDesc(parent.getId());
            if (siblings.isEmpty()) {
                return 1;
            }
            return siblings.get(0).getDisplayOrder() + 1;
        }
    }

    private List<CategoryResponse> buildTreeStructure(List<Category> categories) {
        // ID를 키로 하는 카테고리 맵 생성
        Map<Long, CategoryResponse> categoryMap = new HashMap<>();
        List<CategoryResponse> rootCategories = new ArrayList<>();

        // 먼저 모든 카테고리를 CategoryResponse로 변환
        for (Category category : categories) {
            CategoryResponse response = CategoryResponse.from(category);
            categoryMap.put(category.getId(), response);
        }

        // 트리 구조 구성
        for (Category category : categories) {
            CategoryResponse response = categoryMap.get(category.getId());

            if (category.getParent() == null) {
                // 루트 카테고리
                rootCategories.add(response);
            } else {
                // 하위 카테고리 - 부모에 연결
                CategoryResponse parent = categoryMap.get(category.getParent().getId());
                if (parent != null) {
                    parent.addChild(response);
                }
            }
        }

        // 정렬
        sortCategoriesRecursively(rootCategories);
        return rootCategories;
    }

    private void sortCategoriesRecursively(List<CategoryResponse> categories) {
        // displayOrder로 정렬
        categories.sort(Comparator.comparing(CategoryResponse::getDisplayOrder));

        // 하위 카테고리들도 재귀적으로 정렬
        for (CategoryResponse category : categories) {
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                sortCategoriesRecursively(category.getChildren());
            }
        }
    }
}