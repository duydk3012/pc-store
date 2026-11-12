package com.datn.pc_store.modules.favorite.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.favorite.dto.FavoriteResponse;
import com.datn.pc_store.modules.favorite.dto.FavoriteStatusResponse;
import com.datn.pc_store.modules.favorite.entity.ProductFavorite;
import com.datn.pc_store.modules.favorite.repository.ProductFavoriteRepository;
import com.datn.pc_store.modules.product.entity.Product;
import com.datn.pc_store.modules.product.enums.ProductStatus;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {
    private final ProductFavoriteRepository favorites;
    private final ProductRepository products;

    public PageResponse<FavoriteResponse> list(Long customerId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        return PageResponse.of(favorites.findVisibleFavoritesByCustomerId(
                customerId, ProductStatus.INACTIVE, pageable));
    }

    public FavoriteStatusResponse getStatus(Long customerId, Long productId) {
        return new FavoriteStatusResponse(productId,
                favorites.existsByCustomerIdAndProductId(customerId, productId));
    }

    @Transactional
    public FavoriteResponse add(Long customerId, Long productId) {
        Product product = findVisibleProduct(productId);
        ProductFavorite favorite = favorites.findByCustomerIdAndProductId(customerId, productId)
                .orElseGet(() -> {
                    ProductFavorite created = new ProductFavorite();
                    created.setCustomerId(customerId);
                    created.setProductId(productId);
                    return favorites.saveAndFlush(created);
                });
        return FavoriteResponse.of(product, favorite);
    }

    @Transactional
    public void remove(Long customerId, Long productId) {
        favorites.deleteByCustomerIdAndProductId(customerId, productId);
    }

    private Product findVisibleProduct(Long productId) {
        return products.findById(productId)
                .filter(product -> product.getStatus() != ProductStatus.INACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }
}
