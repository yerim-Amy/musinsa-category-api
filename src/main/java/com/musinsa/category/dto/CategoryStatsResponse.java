package com.musinsa.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStatsResponse {

    private long totalCount;        // 전체 카테고리 수
    private long rootCount;         // 루트 카테고리 수
    private long leafCount;         // 리프 카테고리 수 (자식이 없는 실제 상품이 들어갈 카테고리)
    private int maxDepth;           // 최대 깊이

    public static CategoryStatsResponse of(long totalCount, long rootCount, long leafCount, int maxDepth) {
        return CategoryStatsResponse.builder()
                .totalCount(totalCount)
                .rootCount(rootCount)
                .leafCount(leafCount)
                .maxDepth(maxDepth)
                .build();
    }
}