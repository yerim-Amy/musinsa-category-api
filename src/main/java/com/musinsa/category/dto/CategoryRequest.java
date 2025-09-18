package com.musinsa.category.dto;


import com.musinsa.category.enums.Gender;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    public interface CreateGroup {}
    public interface UpdateGroup {}

    @NotBlank(groups = CreateGroup.class, message = "카테고리명은 필수입니다")
    @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
    private String name;

    @Size(max = 500, groups = {CreateGroup.class, UpdateGroup.class}, message = "설명은 500자를 초과할 수 없습니다")
    private String description;

    private Long parentId;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class}, message = "정렬 순서는 0 이상이어야 합니다")
    private Integer displayOrder;

    private Gender gender = Gender.A;
}