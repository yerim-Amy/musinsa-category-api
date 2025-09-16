package com.musinsa.category.controller;

import com.musinsa.category.common.ApiResponse;
import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.dto.CategoryResponse;
import com.musinsa.category.dto.CategoryStatsResponse;
import com.musinsa.category.exception.BusinessException;
import com.musinsa.category.exception.ErrorCode;
import com.musinsa.category.security.JwtUtil;
import com.musinsa.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "카테고리 관리 API")
public class CategoryController {

    private final CategoryService categoryService;
    private final JwtUtil jwtUtil;

    /**
     * 카테고리 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다")
    public ApiResponse<CategoryResponse> createCategory(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CategoryRequest request) {
        String authHeader = httpRequest.getHeader("Authorization");
        String adminId = validateTokenAndGetAdminId(authHeader);
        log.info("카테고리 생성 요청: {}", request.getName());

        CategoryResponse response = categoryService.createCategory(request, adminId);
        return ApiResponse.success(response, "카테고리가 성공적으로 생성되었습니다");
    }

    /**
     * 카테고리 수정
     */
    @PutMapping("/{id}")
    @Operation(summary = "카테고리 수정", description = "카테고리를 수정합니다")
    public ApiResponse<CategoryResponse> updateCategory(
            HttpServletRequest httpRequest,
            @Parameter(description = "카테고리 ID") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        String authHeader = httpRequest.getHeader("Authorization");
        String adminId = validateTokenAndGetAdminId(authHeader);
        log.info("카테고리 수정 요청 - ID: {}, name: {}", id, request.getName());

        CategoryResponse response = categoryService.updateCategory(id, request, adminId);
        return ApiResponse.success(response, "카테고리가 성공적으로 수정되었습니다");
    }

    /**
     * 카테고리 삭제 (비활성화 처리)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제(비활성화)합니다")
    public ApiResponse<Void> deleteCategory(
            HttpServletRequest httpRequest,
            @Parameter(description = "카테고리 ID") @PathVariable Long id) {
        String authHeader = httpRequest.getHeader("Authorization");
        String adminId = validateTokenAndGetAdminId(authHeader);
        log.info("카테고리 삭제 요청 - ID: {} by {}", id, adminId);

        categoryService.deleteCategory(id, adminId);
        return ApiResponse.success(null, "카테고리가 성공적으로 삭제되었습니다");
    }

    /**
     * 카테고리 활성화
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "카테고리 활성화", description = "비활성화된 카테고리를 활성화합니다")
    public ApiResponse<Void> activateCategory(
            HttpServletRequest httpRequest,
            @Parameter(description = "카테고리 ID") @PathVariable Long id) {
        String authHeader = httpRequest.getHeader("Authorization");
        String adminId = validateTokenAndGetAdminId(authHeader);
        log.info("카테고리 활성화 요청 - ID: {} by {}", id, adminId);
        categoryService.activateCategory(id, adminId);
        return ApiResponse.success(null, "카테고리가 성공적으로 활성화되었습니다");
    }

    // ==== 조회 API들 ====
    /**
     * 단일 카테고리 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "카테고리 조회", description = "특정 카테고리를 조회합니다")
    public ApiResponse<CategoryResponse> getCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long id) {

        CategoryResponse response = categoryService.getCategoryById(id);
        return ApiResponse.success(response);
    }

    /**
     * 특정 카테고리의 직계 하위 카테고리 조회
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "하위 카테고리 조회", description = "특정 카테고리의 직계 하위 카테고리들을 조회합니다")
    public ApiResponse<List<CategoryResponse>> getDirectChildren(
            @Parameter(description = "부모 카테고리 ID") @PathVariable Long id) {

        List<CategoryResponse> children = categoryService.getDirectChildren(id);
        return ApiResponse.success(children);
    }

    /**
     * 카테고리 트리 구조 조회
     * - id가 없으면: 전체 카테고리 트리
     * - id가 있으면: 해당 카테고리를 루트로 하는 하위 트리
     */
    @GetMapping("/tree")
    @Operation(summary = "카테고리 트리 조회", description = "카테고리 트리 구조를 조회합니다")
    public ApiResponse<List<CategoryResponse>> getCategoryTree(
            @Parameter(description = "루트 카테고리 ID (없으면 전체 트리)")
            @RequestParam(required = false) Long categoryId) {

        List<CategoryResponse> tree = categoryService.getCategoryTree(categoryId);
        return ApiResponse.success(tree);
    }

    /**
     * 루트 카테고리들 조회
     */
    @GetMapping("/roots")
    @Operation(summary = "루트 카테고리 조회", description = "최상위 카테고리들을 조회합니다")
    public ApiResponse<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> roots = categoryService.getRootCategories();
        return ApiResponse.success(roots);
    }

    /**
     * 전체 카테고리 조회 (평면 구조)
     */
    @GetMapping
    @Operation(summary = "전체 카테고리 조회", description = "모든 활성 카테고리를 조회합니다")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> all = categoryService.getAllCategories();
        return ApiResponse.success(all);
    }

    /**
     * 카테고리 검색
     */
    @GetMapping("/search")
    @Operation(summary = "카테고리 검색", description = "이름으로 카테고리를 검색합니다")
    public ApiResponse<List<CategoryResponse>> searchCategories(
            @Parameter(description = "검색 키워드 (2자 이상)")
            @RequestParam String keyword) {

        log.info("카테고리 검색 요청 - keyword: {}", keyword);

        List<CategoryResponse> results = categoryService.searchCategories(keyword);
        return ApiResponse.success(results);
    }

    /**
     * 카테고리 간단 통계 조회
     */
    @GetMapping("/stats")
    @Operation(summary = "카테고리 통계", description = "전체,루트,리프 카테고리 수와 최대 깊이를 조회합니다")
    public ApiResponse<CategoryStatsResponse> getCategoryStats() {
        CategoryStatsResponse stats = categoryService.getCategoryStats();
        return ApiResponse.success(stats);
    }

    /**
     * 토큰 검증 및 관리자 ID 추출
     */
    private String validateTokenAndGetAdminId(String authHeader) {
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        if (token == null) {
            throw new BusinessException(ErrorCode.MISSING_TOKEN);
        }

        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        return jwtUtil.extractAdminId(token);
    }
}