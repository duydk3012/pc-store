package com.datn.pc_store.modules.review.repository;

import com.datn.pc_store.modules.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    Page<ProductReview> findByProductId(Long productId, Pageable pageable);
    Optional<ProductReview> findByIdAndProductId(Long id, Long productId);
    boolean existsByProductIdAndCustomerId(Long productId, Long customerId);
}
