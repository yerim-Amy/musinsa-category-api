package com.musinsa.category.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
    private String name;            // 카테고리 이름

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;     // 카테고리 설명

    private Long parentId;          // 부모 카테고리 ID (null이면 최상위)

    @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다")
    private Integer displayOrder;   // 정렬 순서
}