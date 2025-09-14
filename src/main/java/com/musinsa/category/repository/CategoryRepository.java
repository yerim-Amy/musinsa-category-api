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

    // 모든 카테고리 조회
    List<Category> findAllByIsActiveTrue();

    // 특정 부모의 자식 카테고리들만 조회
    List<Category> findAllByParentIdAndIsActiveTrue(Long parentId);

    // 최상위 카테고리들만 조회 (부모가 없는 것들)
    List<Category> findAllByParentIsNullAndIsActiveTrue();

    // 특정 경로로 시작하는 하위 카테고리들 조회
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.path LIKE CONCAT(:parentPath, '/%') ORDER BY c.displayOrder")
    List<Category> findByPathPrefix(@Param("parentPath") String parentPath);

    // ID로 활성화된 카테고리만 조회
    Optional<Category> findByIdAndIsActiveTrue(Long id);
}



