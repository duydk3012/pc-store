package com.datn.pc_store.modules.review.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.DuplicateResourceException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.repository.OrderItemRepository;
import com.datn.pc_store.modules.product.enums.ProductStatus;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import com.datn.pc_store.modules.review.dto.ReviewRequest;
import com.datn.pc_store.modules.review.dto.ReviewResponse;
import com.datn.pc_store.modules.review.entity.ProductReview;
import com.datn.pc_store.modules.review.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ProductReviewRepository reviews;
    private final ProductRepository products;
    private final OrderItemRepository orderItems;

    public PageResponse<ReviewResponse> list(Long productId, int page, int size) {
        requireVisibleProduct(productId);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        return PageResponse.of(reviews.findByProductId(productId, pageable).map(ReviewResponse::of));
    }

    public ReviewResponse get(Long productId, Long id) {
        return ReviewResponse.of(findReview(productId, id));
    }

    @Transactional
    public ReviewResponse create(Long productId, Long customerId, ReviewRequest request) {
        requireVisibleProduct(productId);
        requireCompletedPurchase(productId, customerId);
        if (reviews.existsByProductIdAndCustomerId(productId, customerId)) {
            throw new DuplicateResourceException("Bạn đã đánh giá sản phẩm này");
        }
        var review = new ProductReview();
        review.setProductId(productId);
        review.setCustomerId(customerId);
        return save(review, request);
    }

    @Transactional
    public ReviewResponse update(Long productId, Long id, Long customerId, ReviewRequest request) {
        var review = findReview(productId, id);
        if (!review.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("Chỉ người viết được sửa đánh giá");
        }
        requireCompletedPurchase(productId, customerId);
        return save(review, request);
    }

    @Transactional
    public void delete(Long productId, Long id, CustomUserDetails actor) {
        var review = findReview(productId, id);
        boolean admin = actor.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!admin && !review.getCustomerId().equals(actor.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa đánh giá này");
        }
        reviews.delete(review);
    }

    private void requireVisibleProduct(Long productId) {
        products.findById(productId).filter(product -> product.getStatus() != ProductStatus.INACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }

    private ProductReview findReview(Long productId, Long id) {
        requireVisibleProduct(productId);
        return reviews.findByIdAndProductId(id, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));
    }

    private void requireCompletedPurchase(Long productId, Long customerId) {
        if (!orderItems.existsByProductIdAndOrder_UserIdAndOrder_Status(productId, customerId, OrderStatus.COMPLETED)) {
            throw new AccessDeniedException("Chỉ được đánh giá sản phẩm trong đơn hàng đã hoàn tất của bạn");
        }
    }

    private ReviewResponse save(ProductReview review, ReviewRequest request) {
        review.setRating(request.rating().byteValueExact());
        review.setContent(request.content() == null ? null : request.content().strip());
        return ReviewResponse.of(reviews.saveAndFlush(review));
    }
}
