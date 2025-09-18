package com.musinsa.category.entity;

import com.musinsa.category.enums.Gender;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

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

    @OneToMany(mappedBy = "parent")
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 1;

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

    public void updateInfo( String name, String description, Integer displayOrder, Gender gender) {
        if (StringUtils.hasText(name)) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim().isEmpty() ? null : description.trim();
        }
        if (displayOrder != null && 0 < displayOrder) {
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
        this.children.forEach(Category::updatePathAndDepth);
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
        // 기존 부모에서 제거
        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        this.parent = newParent;

        // 새 부모에 추가
        if (newParent != null) {
            newParent.children.add(this);
        }
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