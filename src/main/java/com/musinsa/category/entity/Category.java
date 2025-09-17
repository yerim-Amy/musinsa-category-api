package com.musinsa.category.entity;

import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.enums.Gender;
import com.musinsa.category.exception.BusinessException;
import com.musinsa.category.exception.ErrorCode;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(name = "uk_category_parent_display_order",columnNames = {"parent_id", "display_order"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 1)
    @Builder.Default
    private Gender gender = Gender.A;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer depth = 0;

    @Column(length = 1000)
    private String path;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    public void updateInfo(CategoryRequest request) {
        String name = request.getName();
        String description= request.getDescription();
        Integer displayOrder =request.getDisplayOrder();
        Gender gender = request.getGender();

        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim().isEmpty() ? null : description.trim();
        }
        if (displayOrder != null && 0 <= displayOrder) {
            this.displayOrder = displayOrder;
        }
        if(gender != null){
            this.gender = gender;
        }
    }

    public void deactivate() {
        this.isActive = false;
        // 하위 카테고리도 전부 비활성화
        this.children.forEach(Category::deactivate);
    }

    public void activate() {
        this.isActive = true;
    }

    public void updatePathAndDepth() {
        if (this.parent != null) {
            this.path = this.parent.getPath() + "/" + this.id;
            this.depth = this.parent.getDepth() + 1;
        } else {
            this.path = "/" + this.id;
            this.depth = 0;
        }
        for (Category child : this.children) {
            child.updatePathAndDepth();
        }
    }

    public void updateAuditInfo(String userId) {
        if (this.createdBy == null) {
            this.createdBy = userId;
            this.createdAt = LocalDateTime.now();
        }
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setParent(Category newParent) {
        // 자신을 부모로 설정하는 경우 방지
        if (newParent != null && newParent.equals(this)) {
            throw new BusinessException(ErrorCode.CATEGORY_SELF_PARENT);
        }
        // 모든 하위를 부모로 설정하는 경우 방지 (직계 + 후손 체크)
        if (newParent != null && newParent.isDescendantOf(this)) {
            throw new BusinessException(ErrorCode.CATEGORY_INVALID_PARENT);
        }

        this.parent = newParent;
        updatePathAndDepth();
    }

    // path 기반으로 하위인지 체크
    public boolean isDescendantOf(Category potentialAncestor) {
        if (potentialAncestor == null || this.path == null || potentialAncestor.getPath() == null) {
            return false;
        }

        String ancestorPath = potentialAncestor.getPath();
        return this.path.startsWith(ancestorPath + "/");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) {
            return false;
        }
        Category category = (Category) o;
        return id != null && id.equals(category.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Category{id=%d, name='%s', depth=%d, path='%s'}",
                id, name, depth, path);
    }
}