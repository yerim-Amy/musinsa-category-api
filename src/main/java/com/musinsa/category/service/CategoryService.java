package com.musinsa.category.service;

import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.dto.CategoryResponse;
import com.musinsa.category.entity.Category;
import com.musinsa.category.enums.Gender;
import com.musinsa.category.exception.BusinessException;
import com.musinsa.category.exception.ErrorCode;
import com.musinsa.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final int MIN_SEARCH_KEYWORD_LENGTH = 2;
    private static final int MIN_DISPLAY_ORDER = 1;

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, String adminId) {
        log.info("카테고리 생성 요청 - name: {}, parentId: {}, adminId: {}, gender: {}",
                request.getName(), request.getParentId(), adminId, request.getGender());

        // 입력값 검증
        if (!StringUtils.hasText(request.getName())) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_REQUIRED);
        }

        String trimmedName = request.getName().trim();
        validateNameLength(trimmedName);

        // 이름 중복 검증
        Long requestParentId = request.getParentId();
        validateNameDuplication(trimmedName, requestParentId);

        // 부모 카테고리 검증
        Category parent = null;
        if (requestParentId != null) {
            parent = getParentById(requestParentId);
            if (MAX_CATEGORY_DEPTH <= parent.getDepth()) {
                throw new BusinessException(ErrorCode.CATEGORY_DEPTH_EXCEEDED, "최대 깊이 : " + MAX_CATEGORY_DEPTH);
            }
        }

        // 설정할 displayOrder 계산
        Integer displayOrder = calculateDisplayOrder(request, requestParentId);

        Category category = Category.builder()
                .name(trimmedName)
                .description(request.getDescription())
                .displayOrder(displayOrder)
                .gender(request.getGender())
                .isActive(true)
                .createdBy(adminId)
                .updatedBy(adminId)
                .build();

        Category savedCategory = categoryRepository.save(category);
        // 부모-자식 관계 설정
        if (parent != null) {
            savedCategory.setParent(parent);
        }
        savedCategory.updatePathAndDepth();

        log.info("카테고리 생성 완료 - ID: {}, Name: '{}', Path: {}, DisplayOrder: {}, adminId: {}",
                savedCategory.getId(), savedCategory.getName(), savedCategory.getPath(),
                savedCategory.getDisplayOrder(), adminId);

        return CategoryResponse.from(savedCategory);
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, String adminId) {
        log.info("카테고리 수정 요청 - ID: {}, adminId: {}", categoryId, adminId);

        Category category = getActiveCategoryById(categoryId);
        Long currentParentId = category.getParent() != null ? category.getParent().getId() : null;

        // 변경할 이름 이미 존재하는지 검증
        String requestName = request.getName();
        if (requestName != null && !requestName.equals(category.getName())) {
            validateNameLength(requestName);
            validateNameDuplication(requestName, currentParentId);
        }

        // 부모 카테고리 변경 시
        Category newParent = getNewParent(request, category, currentParentId);
        Long newParentId = newParent != null ? newParent.getId() : currentParentId;

        // 설정할 displayOrder 계산
        Integer displayOrder = calculateDisplayOrder(request, newParentId);

        // 요청한 카테고리 정보 업데이트
        category.updateInfo(requestName, request.getDescription(), displayOrder, request.getGender());
        category.updateAuditInfo(adminId);

        if (newParent!=null) {
            category.setParent(newParent);
            category.updatePathAndDepth();
        }

        log.info("카테고리 수정 완료 - ID: {}, adminId: {}", categoryId, adminId);
        return CategoryResponse.from(category);
    }

    /**
     * 카테고리 삭제 (비활성화)
     */
    @Transactional
    public void deleteCategory(Long categoryId, String adminId) {
        log.info("카테고리 삭제 요청 - ID: {}, adminId: {}", categoryId, adminId);

        Category category = getActiveCategoryById(categoryId);
        validateHasNoChildren(categoryId);

        category.deactivate();
        category.updateAuditInfo(adminId);

        log.info("카테고리 삭제 완료 - ID: {}, adminId: {}", categoryId, adminId);
    }

    /**
     * 카테고리 삭제 (물리적 삭제)
     */
    @Transactional
    public void realDeleteCategory(Long categoryId, String adminId) {
        log.warn("카테고리 물리적 삭제 요청 - ID: {}, adminId: {}", categoryId, adminId);

        Category category = getActiveCategoryById(categoryId);
        validateHasNoChildren(categoryId);

        categoryRepository.delete(category);
        log.warn("카테고리 물리적 삭제 완료 - ID: {}, adminId: {}", categoryId, adminId);
    }

    /**
     * 카테고리 활성화
     */
    @Transactional
    public void activateCategory(Long categoryId, String adminId) {
        log.info("카테고리 활성화 요청 - ID: {}, adminId: {}", categoryId, adminId);

        Category category =  categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 부모가 비활성 상태면 활성화 불가
        if (category.getParent() != null && !category.getParent().getIsActive()) {
            throw new BusinessException(ErrorCode.CATEGORY_INACTIVE_PARENT);
        }

        if(category.getIsActive()){
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_ACTIVE);
        }

        category.activate();
        category.updateAuditInfo(adminId);

        log.info("카테고리 활성화 완료 - ID: {}, adminId: {}", categoryId, adminId);
    }

    /**
     * 카테고리 단일 조회
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        log.debug("카테고리 조회 - ID: {}", categoryId);
        Category category = getActiveCategoryById(categoryId);
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
            children = categoryRepository.findRootCategories(null);
        } else {
            // 부모 존재 여부 확인 후, 하위 카테고리들 조회
            getParentById(parentId);
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
    public List<CategoryResponse> getCategoryTree(Long categoryId, Gender gender) {
        log.debug("카테고리 트리 조회 - ID: {}, Gender: {}", categoryId, gender);

        List<Category> categories;

        // 1. 카테고리 조회
        if (categoryId == null) {
            categories = categoryRepository.findAllActiveWithParent(gender.name());
        } else {
            Category rootCategory = getActiveCategoryById(categoryId);
            categories = categoryRepository.findDescendants(rootCategory.getPath(),gender.name());
            categories.add(0, rootCategory); // 본인도 포함
        }

        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. ID를 키로 하는 CategoryResponse Map 생성
        Map<Long, CategoryResponse> categoryMap = new HashMap<>();
        for (Category category : categories) {
            CategoryResponse response = CategoryResponse.from(category);
            categoryMap.put(category.getId(), response);
        }

        // 3. 트리 구조 생성
        List<CategoryResponse> rootCategories = new ArrayList<>();
        for (Category category : categories) {
            CategoryResponse response = categoryMap.get(category.getId());

            // 루트 여부 판단: 특정 ID 조회 시 or 전체 조회
            boolean isRoot = (category.getId().equals(categoryId))
                    || (categoryId == null && category.getParent() == null);
            if (isRoot) {
                rootCategories.add(response);
            } else if (category.getParent() != null) {
                // 부모-자식 연결
                CategoryResponse parent = categoryMap.get(category.getParent().getId());
                if (parent != null) {
                    parent.addChild(response);
                }
            }
        }

        sortCategoryByDisplayOrder(rootCategories);
        return rootCategories;
    }

    /**
     * 루트 카테고리들 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories(Gender gender) {
        log.debug("루트 카테고리 조회 - Gender: {}", gender);

        List<Category> rootCategories = categoryRepository.findRootCategories(gender.name());
        return rootCategories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 전체 카테고리 조회 (활성화된 것만)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Gender gender) {
        log.debug("전체 카테고리 조회 - Gender: {}", gender);

        List<Category> categories = categoryRepository.findAllActiveOrdered(gender.name());
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

        if (!StringUtils.hasText(keyword) || keyword.trim().length() < MIN_SEARCH_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Category> categories = categoryRepository.searchByName(keyword.trim());
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    private void validateNameLength(String name) {
        String trimmedName = name.trim();
        if (MAX_CATEGORY_NAME_LENGTH < trimmedName.length()) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_TOO_LONG);
        }
    }

    private void validateNameDuplication(String name, Long parentId) {
        if (categoryRepository.existsByNameAndParent(name.trim(), parentId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    private void validateHasNoChildren(Long categoryId) {
        List<Category> children = categoryRepository.findChildrenByParentId(categoryId);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }
    }

    private Category getActiveCategoryById(Long categoryId) {
        return categoryRepository.findActiveById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Category getParentById(Long parentId) {
        Optional<Category> parentOptional = categoryRepository.findActiveById(parentId);
        if (parentOptional.isEmpty()) {
            String errMessage = String.format("입력한 parentId %d에 해당하는 부모 카테고리가 없습니다.", parentId);
            throw new BusinessException(ErrorCode.CATEGORY_PARENT_NOT_FOUND, errMessage);
        }
        return parentOptional.get();
    }

    private Category getNewParent(CategoryRequest request, Category category, Long currentParentId){
        Long requestParentId = request.getParentId();
        if (requestParentId != null && !requestParentId.equals(currentParentId)) {
            Category newParent = getParentById(requestParentId);
            // 자신을 부모로 설정하는 경우 방지
            if (newParent.equals(category)) {
                throw new BusinessException(ErrorCode.CATEGORY_SELF_PARENT);
            }
            // 모든 하위를 부모로 설정하는 경우 방지 (직계 + 후손 체크)
            if (newParent.isDescendantOf(category)) {
                throw new BusinessException(ErrorCode.CATEGORY_INVALID_PARENT);
            }
            return newParent;
        }
        return null;
    }

    private Integer getNextDisplayOrder(Long parentId, Gender gender) {
        List<Category> siblings = (parentId == null)
                ? categoryRepository.findRootCategories(gender.name())        // 루트 레벨의 다음 순서
                : categoryRepository.findChildrenByParentId(parentId);  // 특정 부모의 하위 레벨의 다음 순서
        return siblings.isEmpty() ? 1 :
                siblings.stream().mapToInt(Category::getDisplayOrder).max().orElse(0) + 1;
    }

    private Integer calculateDisplayOrder(CategoryRequest request, Long parentId) {
        Integer displayOrder = request.getDisplayOrder();

        if (displayOrder != null) {
            if (displayOrder < MIN_DISPLAY_ORDER) {
                throw new BusinessException(ErrorCode.INVALID_DISPLAY_ORDER);
            }

            Optional<Category> existingCategory = categoryRepository.findByParentIdAndDisplayOrder(parentId, displayOrder);
            if (existingCategory.isPresent()) {
                String conflictInfo = String.format("'%s' 카테고리", existingCategory.get().getName());
                String errorMessage = String.format("순서 %d번은 이미 %s에서 사용 중입니다. 다른 순서를 선택해주세요.",
                        displayOrder, conflictInfo);
                throw new BusinessException(ErrorCode.DISPLAY_ORDER_DUPLICATE, errorMessage);
            }
            return displayOrder;
        }

        // displayOrder 설정이 없으면 자동 할당
        Integer nextOrder = getNextDisplayOrder(parentId, request.getGender());
        log.debug("displayOrder 자동 할당 - parentId: {}, 할당된 순서: {}", parentId, nextOrder);
        return nextOrder;
    }


    private void sortCategoryByDisplayOrder(List<CategoryResponse> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        categories.sort(Comparator.comparing(CategoryResponse::getDisplayOrder));

        // 하위 카테고리들도 정렬
        for (CategoryResponse category : categories) {
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                sortCategoryByDisplayOrder(category.getChildren());
            }
        }
    }
}