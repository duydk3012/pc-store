package com.datn.pc_store.modules.comment.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_comments")
@Getter @Setter @NoArgsConstructor
public class ProductComment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "parent_id")
    private Long parentId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
}
