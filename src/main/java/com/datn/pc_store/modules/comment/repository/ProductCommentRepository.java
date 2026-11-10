package com.datn.pc_store.modules.comment.repository;

import com.datn.pc_store.modules.comment.entity.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.Optional;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {
    Optional<ProductComment> findByIdAndProductId(Long id, Long productId);
    Page<ProductComment> findByProductIdAndParentIdIsNull(Long productId, Pageable pageable);
    Page<ProductComment> findByProductIdAndParentId(Long productId, Long parentId, Pageable pageable);
}
