package com.datn.pc_store.modules.comment.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.*;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.comment.dto.*;
import com.datn.pc_store.modules.comment.entity.ProductComment;
import com.datn.pc_store.modules.comment.repository.ProductCommentRepository;
import com.datn.pc_store.modules.product.enums.ProductStatus;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final ProductCommentRepository comments;
    private final ProductRepository products;

    private void checkProduct(Long productId) {
        products.findById(productId).filter(p -> p.getStatus() != ProductStatus.INACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }

    private ProductComment find(Long productId, Long id) {
        checkProduct(productId);
        return comments.findByIdAndProductId(id, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));
    }

    private CustomUserDetails actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            throw new AccessDeniedException("Yêu cầu đăng nhập");
        }
        return user;
    }

    private void requireOwner(ProductComment c, boolean allowAdmin) {
        var user = actor();
        boolean admin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!Objects.equals(c.getCreatedBy(), user.getId()) && !(allowAdmin && admin)) {
            throw new AccessDeniedException("Bạn không có quyền thay đổi bình luận này");
        }
    }

    public PageResponse<CommentResponse> list(Long productId, Long parentId, int page, int size) {
        checkProduct(productId);
        if (parentId != null) find(productId, parentId);
        var pageable = PageRequest.of(page, size, Sort.by(parentId == null ? Sort.Direction.DESC : Sort.Direction.ASC,
                "createdDate", "id"));
        var result = parentId == null ? comments.findByProductIdAndParentIdIsNull(productId, pageable)
                : comments.findByProductIdAndParentId(productId, parentId, pageable);
        return PageResponse.of(result.map(CommentResponse::of));
    }

    public CommentResponse get(Long productId, Long id) {
        return CommentResponse.of(find(productId, id));
    }

    @Transactional
    public CommentResponse create(Long productId, Long parentId, CommentRequest request) {
        var user = actor();
        checkProduct(productId);
        if (parentId != null && find(productId, parentId).isDeleted()) {
            throw new BadRequestException("Không thể trả lời bình luận đã xóa");
        }
        var c = new ProductComment();
        c.setProductId(productId);
        c.setParentId(parentId);
        c.setContent(request.content().strip());
        c.setCreatedBy(user.getId());
        c.setLastModifiedBy(user.getId());
        return CommentResponse.of(comments.saveAndFlush(c));
    }

    @Transactional
    public CommentResponse update(Long productId, Long id, CommentRequest request) {
        var c = find(productId, id);
        requireOwner(c, false);
        if (c.isDeleted()) throw new BadRequestException("Bình luận đã bị xóa");
        c.setContent(request.content().strip());
        return CommentResponse.of(comments.saveAndFlush(c));
    }

    @Transactional
    public void delete(Long productId, Long id) {
        var c = find(productId, id);
        requireOwner(c, true);
        c.setDeleted(true);
        c.setContent("");
        comments.saveAndFlush(c);
    }
}
