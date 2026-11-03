package com.datn.pc_store.modules.product.dto;

import com.datn.pc_store.modules.product.enums.ProductStatus;
import com.datn.pc_store.modules.product.enums.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin phản hồi của sản phẩm")
public class ProductResponse {
    @Schema(description = "ID sản phẩm", example = "1")
    private Long id;
    @Schema(description = "Tên sản phẩm", example = "Card màn hình ASUS RTX 5070")
    private String name;
    @Schema(description = "Mã sản phẩm duy nhất", example = "ASUS-RTX5070-12G")
    private String code;
    @Schema(description = "Đường dẫn thân thiện", example = "card-man-hinh-asus-rtx-5070")
    private String slug;
    @Schema(description = "Mô tả sản phẩm", example = "Card đồ họa dành cho chơi game và thiết kế")
    private String description;
    @Schema(description = "Thông số kỹ thuật", example = "VRAM: 12GB GDDR7")
    private String specification;
    @Schema(description = "Loại sản phẩm", example = "OTHER")
    private ProductType productType;
    @Schema(description = "Trạng thái sản phẩm", example = "ACTIVE")
    private ProductStatus status;
    @Schema(description = "Đường dẫn ảnh sản phẩm", example = "https://example.com/images/rtx5070.jpg")
    private String imageUrl;
    @Schema(description = "ID thương hiệu", example = "1")
    private Long brandId;
    @Schema(description = "Danh sách ID danh mục", example = "[1, 2]")
    private Set<Long> categoryIds;
    @Schema(description = "ID danh mục chính", example = "1")
    private Long primaryCategoryId;
    @Schema(description = "Giá bán hiện tại lưu trực tiếp ở products.price, VND; null nếu chưa gán giá", example = "12990000")
    private Long price;
    @Schema(description = "Danh sách linh kiện cấu hình (áp dụng cho sản phẩm loại PC); null nếu là sản phẩm khác")
    private List<BuildingPcItemResponse> items;
    @Schema(description = "Thời gian tạo")
    private Instant createdDate;
    @Schema(description = "ID người tạo")
    private Long createdBy;
    @Schema(description = "Thời gian cập nhật gần nhất")
    private Instant lastModifiedDate;
    @Schema(description = "ID người cập nhật gần nhất")
    private Long lastModifiedBy;
}
