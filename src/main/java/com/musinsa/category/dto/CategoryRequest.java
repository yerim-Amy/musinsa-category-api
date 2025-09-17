package com.musinsa.category.dto;


import com.musinsa.category.enums.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequest {

    @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;

    private Long parentId;

    @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다")
    private Integer displayOrder;

    private Gender gender = Gender.A;
}