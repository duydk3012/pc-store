package com.datn.pc_store.modules.favorite.dto;

import com.datn.pc_store.modules.favorite.entity.ProductFavorite;
import com.datn.pc_store.modules.product.entity.Product;
import com.datn.pc_store.modules.product.enums.ProductStatus;

import java.time.Instant;

public record FavoriteResponse(
        Long productId,
        String name,
        String code,
        String slug,
        String imageUrl,
        Long price,
        ProductStatus status,
        Instant addedAt) {

    public static FavoriteResponse of(Product product, ProductFavorite favorite) {
        return new FavoriteResponse(product.getId(), product.getName(), product.getCode(), product.getSlug(),
                product.getImageUrl(), product.getPrice(), product.getStatus(), favorite.getCreatedAt());
    }
}
