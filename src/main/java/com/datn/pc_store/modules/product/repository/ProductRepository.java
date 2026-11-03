package com.datn.pc_store.modules.product.repository;

import com.datn.pc_store.modules.product.entity.Product;
import com.datn.pc_store.modules.product.enums.ProductStatus;
import com.datn.pc_store.modules.product.enums.ProductType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id=:id")
    Optional<Product> findForUpdate(@Param("id") Long id);

    Optional<Product> findBySlug(String slug);

    // Include soft-deleted products: their unique code and slug remain reserved.
    @Query(value = "SELECT COUNT(*) FROM products WHERE code = :code AND (:id IS NULL OR id <> :id)", nativeQuery = true)
    long countByCodeIncludingDeleted(@Param("code") String code, @Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM products WHERE slug = :slug AND (:id IS NULL OR id <> :id)", nativeQuery = true)
    long countBySlugIncludingDeleted(@Param("slug") String slug, @Param("id") Long id);

    @Query("""
        SELECT p FROM Product p
        WHERE (:status IS NULL OR p.status = :status)
          AND (:publicOnly = false OR p.status <> com.datn.pc_store.modules.product.enums.ProductStatus.INACTIVE)
          AND (:productType IS NULL OR p.productType = :productType)
          AND (:brandId IS NULL OR p.brandId = :brandId)
          AND (:categoryId IS NULL OR EXISTS
               (SELECT pc.id FROM ProductCategory pc WHERE pc.productId = p.id AND pc.categoryId = :categoryId))
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("status") ProductStatus status,
            @Param("productType") ProductType productType,
            @Param("brandId") Long brandId,
            @Param("categoryId") Long categoryId,
            @Param("publicOnly") boolean publicOnly,
            Pageable pageable
    );
}
