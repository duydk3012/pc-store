package com.datn.pc_store.modules.review.dto;

import com.datn.pc_store.modules.review.entity.ProductReview;
import java.time.Instant;

public record ReviewResponse(Long id, Long productId, Long customerId, int rating, String content,
                             Instant createdDate, Instant lastModifiedDate) {
    public static ReviewResponse of(ProductReview review) {
        return new ReviewResponse(review.getId(), review.getProductId(), review.getCustomerId(),
                review.getRating(), review.getContent(), review.getCreatedDate(), review.getLastModifiedDate());
    }
}
