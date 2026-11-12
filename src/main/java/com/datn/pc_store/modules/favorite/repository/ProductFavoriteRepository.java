package com.datn.pc_store.modules.favorite.repository;

import com.datn.pc_store.modules.favorite.dto.FavoriteResponse;
import com.datn.pc_store.modules.favorite.entity.ProductFavorite;
import com.datn.pc_store.modules.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
    Optional<ProductFavorite> findByCustomerIdAndProductId(Long customerId, Long productId);

    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);

    void deleteByCustomerIdAndProductId(Long customerId, Long productId);

    @Query(value = """
            SELECT new com.datn.pc_store.modules.favorite.dto.FavoriteResponse(
                p.id, p.name, p.code, p.slug, p.imageUrl, p.price, p.status, f.createdAt)
            FROM ProductFavorite f JOIN Product p ON p.id = f.productId
            WHERE f.customerId = :customerId AND p.status <> :inactiveStatus
            ORDER BY f.createdAt DESC, f.id DESC
            """,
            countQuery = """
            SELECT COUNT(f)
            FROM ProductFavorite f JOIN Product p ON p.id = f.productId
            WHERE f.customerId = :customerId AND p.status <> :inactiveStatus
            """)
    Page<FavoriteResponse> findVisibleFavoritesByCustomerId(
            @Param("customerId") Long customerId,
            @Param("inactiveStatus") ProductStatus inactiveStatus,
            Pageable pageable);
}
