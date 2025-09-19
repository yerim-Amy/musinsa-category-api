package com.musinsa.category.repository;

import com.musinsa.category.entity.Category;
import com.musinsa.category.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    String GENDER_CONDITION = " ((:gender = 'A' AND 1=1) OR (c.gender = :gender OR c.gender = 'A' OR c.gender IS NULL)) ";
    String PARENT_CONDITION = " ((:parentId IS NULL AND c.parent IS NULL) OR (c.parent.id = :parentId)) ";

    // =========== 조회 ===========

    /**
     * ID로 활성화된 카테고리 조회
     */
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.isActive = true")
    Optional<Category> findActiveById(@Param("id") Long id);

    /**
     * 모든 활성화된 카테고리 조회하고 정렬  (depth, displayOrder 순)
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND" +
            GENDER_CONDITION + "ORDER BY c.depth ASC, c.displayOrder ASC")
    List<Category> findAllActiveOrdered(@Param("gender") String gender);

    // ======= 부모-자식 관계 조회 =======

    /**
     * 루트 카테고리들 조회 (displayOrder 순)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true AND" +
            GENDER_CONDITION + "ORDER BY c.displayOrder ASC")
    List<Category> findRootCategories(@Param("gender") String gender);

    /**
     * 모든 활성 카테고리 조회 (부모 정보 포함)
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.isActive = true AND" +
            GENDER_CONDITION + "ORDER BY c.path")
    List<Category> findAllActiveWithParent(@Param("gender") String gender);

    /**
     * 특정 부모의 자식 카테고리들 조회 (displayOrder 순)
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 특정 path의 모든 하위 카테고리들 조회
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.path LIKE CONCAT(:parentPath, '/%') AND" +
            GENDER_CONDITION + "ORDER BY c.depth ASC, c.displayOrder ASC")
    List<Category> findDescendants(@Param("parentPath") String parentPath, @Param("gender") String gender);


    // ========= 카테고리 검색 =========

    /**
     * 이름으로 카테고리 검색
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.name LIKE %:keyword% ORDER BY c.name ASC")
    List<Category> searchByName(@Param("keyword") String keyword);

    // ======== 데이터 중복 검증 ========

    /**
     * 같은 부모 하위에서 이름 중복 확인
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
            "WHERE c.name = :name AND c.isActive = true AND" + PARENT_CONDITION)
    boolean existsByNameAndParent(@Param("name") String name, @Param("parentId") Long parentId);
    /**
     * displayOrder 사용 여부와 정보 조회
     */
    @Query("SELECT c FROM Category c WHERE" + PARENT_CONDITION + "AND c.displayOrder = :displayOrder")
    Optional<Category> findByParentIdAndDisplayOrder(@Param("parentId") Long parentId, @Param("displayOrder") Integer displayOrder);
}