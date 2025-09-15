package com.musinsa.category.dto;

import com.musinsa.category.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer displayOrder;
    private Boolean isActive;
    private String path;
    private Integer depth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    @Builder.Default
    private List<CategoryResponse> children = new ArrayList<>();

    // Entity -> Response 변환 (children 포함안함)
    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .path(category.getPath())
                .depth(category.getDepth())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .children(new ArrayList<>()) // 빈 리스트로 초기화
                .build();
    }

    // Entity -> Response 변환 (children 자동 포함) - 기존 호환성
    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .path(category.getPath())
                .depth(category.getDepth())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .children(category.getChildren().stream()
                        .filter(Category::getIsActive)
                        .map(CategoryResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    // 자식 추가 메서드 (Service에서 트리 구조 만들 때 사용)
    public void addChild(CategoryResponse child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
}