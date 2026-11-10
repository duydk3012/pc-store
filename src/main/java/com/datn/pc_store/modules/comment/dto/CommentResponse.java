package com.datn.pc_store.modules.comment.dto;

import com.datn.pc_store.modules.comment.entity.ProductComment;
import java.time.Instant;

public record CommentResponse(Long id, Long productId, Long parentId, String content,
        boolean isDeleted, Long createdBy, Instant createdDate, Long lastModifiedBy, Instant lastModifiedDate) {
    public static CommentResponse of(ProductComment c) {
        return new CommentResponse(c.getId(), c.getProductId(), c.getParentId(),
                c.isDeleted() ? null : c.getContent(), c.isDeleted(), c.getCreatedBy(),
                c.getCreatedDate(), c.getLastModifiedBy(), c.getLastModifiedDate());
    }
}
