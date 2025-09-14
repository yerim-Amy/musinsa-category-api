package com.musinsa.category.repository;

import com.musinsa.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ==== 기본 조회 ====

    /**
     * ID로 활성화된 카테고리 조회
     */
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.isActive = true")
    Optional<Category> findActiveById(@Param("id") Long id);

    /**
     * 모든 활성화된 카테고리 조회
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true")
    List<Category> findAllActive();

    /**
     * 모든 활성화된 카테고리를 정렬해서 조회 (depth, displayOrder 순)
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.depth ASC, c.displayOrder ASC")
    List<Category> findAllActiveOrdered();

    // ==== 부모-자식 관계 조회 ====

    /**
     * 루트 카테고리들 조회 (displayOrder 순)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findRootCategories();

    /**
     * 루트 카테고리들을 역순으로 조회 (최대 displayOrder 찾기용)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder DESC")
    List<Category> findRootCategoriesDesc();

    /**
     * 특정 부모의 자식 카테고리들 조회 (displayOrder 순)
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 특정 부모의 자식 카테고리들을 역순으로 조회 (최대 displayOrder 찾기용)
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.displayOrder DESC")
    List<Category> findChildrenByParentIdDesc(@Param("parentId") Long parentId);

    // ==== path 기반 조회 (트리 구조용) ====

    /**
     * 특정 path의 모든 하위 카테고리들 조회
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.path LIKE CONCAT(:parentPath, '/%') ORDER BY c.depth ASC, c.displayOrder ASC")
    List<Category> findDescendants(@Param("parentPath") String parentPath);

    // ==== 검색 기능 ====

    /**
     * 이름으로 카테고리 검색
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.name LIKE %:keyword% ORDER BY c.name ASC")
    List<Category> searchByName(@Param("keyword") String keyword);

    // ==== 중복 검증 ====

    /**
     * 같은 부모 하위에서 동일한 이름 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
            "WHERE c.name = :name AND c.isActive = true " +
            "AND (:parentId IS NULL AND c.parent IS NULL OR c.parent.id = :parentId)")
    boolean existsByNameAndParent(@Param("name") String name, @Param("parentId") Long parentId);

    /**
     * 같은 부모 하위에서 동일한 이름을 가진 카테고리들 조회 (수정 시 사용)
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.isActive = true " +
            "AND (:parentId IS NULL AND c.parent IS NULL OR c.parent.id = :parentId)")
    List<Category> findByNameAndParent(@Param("name") String name, @Param("parentId") Long parentId);
}