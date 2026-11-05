package com.datn.pc_store.modules.order.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.DuplicateResourceException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.goodsreceipt.entity.ProductSerial;
import com.datn.pc_store.modules.order.dto.*;
import com.datn.pc_store.modules.order.entity.Order;
import com.datn.pc_store.modules.order.entity.OrderItem;
import com.datn.pc_store.modules.order.entity.OrderItemSerial;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.enums.OrderType;
import com.datn.pc_store.modules.order.mapper.OrderMapper;
import com.datn.pc_store.modules.order.repository.OrderItemSerialRepository;
import com.datn.pc_store.modules.order.repository.OrderRepository;
import com.datn.pc_store.modules.order.util.OrderAddressHelper;
import com.datn.pc_store.modules.product.entity.Product;
import com.datn.pc_store.modules.product.enums.ProductStatus;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import com.datn.pc_store.modules.user.entity.User;
import com.datn.pc_store.modules.user.enums.UserStatus;
import com.datn.pc_store.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "code", "userId", "type", "totalAmount", "discountAmount", "finalAmount",
            "status", "address", "createdDate", "createdBy", "lastModifiedDate", "lastModifiedBy"
    );

    private final OrderRepository orderRepository;
    private final OrderItemSerialRepository orderItemSerialRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(
            String keyword,
            OrderStatus status,
            OrderType type,
            Long userId,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    ) {
        validateDateRange(fromDate, toDate);
        for (var sort : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(sort.getProperty())) {
                throw new BadRequestException("Trường sắp xếp không hợp lệ: " + sort.getProperty()
                        + ". Trường cho phép: " + String.join(", ", SORTABLE_FIELDS));
            }
        }

        Page<Order> page = orderRepository.searchOrders(
                keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                status,
                type,
                userId,
                fromDate,
                toDate,
                pageable
        );

        List<OrderResponse> content = page.getContent().stream()
                .map(this::mapOrderWithSerials)
                .toList();

        return PageResponse.of(new org.springframework.data.domain.PageImpl<>(
                content, pageable, page.getTotalElements()
        ));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, Long currentUserId, boolean isStaffOrAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));

        if (!isStaffOrAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin đơn hàng này.");
        }

        return mapOrderWithSerials(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String code, Long currentUserId, boolean isStaffOrAdmin) {
        Order order = orderRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "code", code));

        if (!isStaffOrAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin đơn hàng này.");
        }

        return mapOrderWithSerials(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest req, Long currentUserId, boolean isStaffOrAdmin) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("Đơn hàng phải có ít nhất 1 sản phẩm.");
        }

        // Xác định chủ sở hữu đơn hàng (userId)
        Long targetUserId;
        if (req.getUserId() != null && isStaffOrAdmin) {
            targetUserId = req.getUserId();
        } else if (currentUserId != null) {
            targetUserId = currentUserId;
        } else if (req.getUserId() != null) {
            targetUserId = req.getUserId();
        } else {
            throw new BadRequestException("Vui lòng cung cấp ID khách hàng hoặc đăng nhập để tiếp tục.");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", targetUserId));
        if (user.isDeleted() || user.getStatus() == UserStatus.BANNED) {
            throw new BadRequestException("Tài khoản khách hàng này đang bị khóa hoặc không hợp lệ.");
        }

        // Mã đơn hàng
        String code;
        if (req.getCode() != null && !req.getCode().isBlank()) {
            code = req.getCode().trim();
            if (orderRepository.existsByCode(code)) {
                throw new DuplicateResourceException("Đơn hàng", "code", code);
            }
        } else {
            code = generateUniqueOrderCode();
        }

        // Xử lý các dòng sản phẩm
        List<OrderItem> orderItems = new ArrayList<>();
        long runningTotal = 0L;
        long totalItemDiscounts = 0L;

        for (OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", itemReq.getProductId()));

            if (product.isDeleted() || product.getStatus() == ProductStatus.INACTIVE) {
                throw new BadRequestException("Sản phẩm '" + product.getName() + "' (ID: "
                        + product.getId() + ") đang tạm ngưng kinh doanh.");
            }

            long unitPrice = (itemReq.getUnitPrice() != null && itemReq.getUnitPrice() > 0)
                    ? itemReq.getUnitPrice()
                    : (product.getPrice() != null ? product.getPrice() : 0L);

            int quantity = itemReq.getQuantity() != null && itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;
            long itemDiscount = (itemReq.getDiscountAmount() != null && itemReq.getDiscountAmount() > 0)
                    ? itemReq.getDiscountAmount()
                    : 0L;

            long itemSubtotal = Math.max(0, (unitPrice * quantity) - itemDiscount);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .discountAmount(itemDiscount)
                    .subtotal(itemSubtotal)
                    .build();

            orderItems.add(orderItem);
            runningTotal += (unitPrice * quantity);
            totalItemDiscounts += itemDiscount;
        }

        long orderLevelDiscount = (req.getDiscountAmount() != null && req.getDiscountAmount() > 0)
                ? req.getDiscountAmount()
                : 0L;
        long finalDiscountAmount = totalItemDiscounts + orderLevelDiscount;
        long finalAmount = Math.max(0, runningTotal - finalDiscountAmount);

        // Lưu thông tin người nhận & ghi chú qua OrderAddressHelper
        String encodedAddress = OrderAddressHelper.encodeAddress(
                req.getAddress(),
                req.getRecipientName(),
                req.getRecipientPhone(),
                req.getNote()
        );

        OrderType orderType = req.getType() != null ? req.getType() : OrderType.ONLINE;

        Order order = Order.builder()
                .code(code)
                .userId(targetUserId)
                .type(orderType)
                .totalAmount(runningTotal)
                .discountAmount(finalDiscountAmount)
                .finalAmount(finalAmount)
                .status(OrderStatus.PENDING)
                .address(encodedAddress)
                .build();

        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
            order.getItems().add(oi);
        }

        Order saved = orderRepository.save(order);
        log.info("Đã tạo đơn hàng mới id={}, code={}, userId={}, finalAmount={}",
                saved.getId(), saved.getCode(), saved.getUserId(), saved.getFinalAmount());

        return mapOrderWithSerials(saved);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus, Long currentUserId, boolean isStaffOrAdmin) {
        Order order = orderRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            return mapOrderWithSerials(order);
        }

        // Quyền khách hàng: chỉ được hủy khi đơn còn PENDING
        if (!isStaffOrAdmin) {
            if (!order.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("Bạn không có quyền cập nhật trạng thái đơn hàng này.");
            }
            if (newStatus != OrderStatus.CANCELLED) {
                throw new BadRequestException("Khách hàng chỉ được phép yêu cầu hủy đơn hàng.");
            }
            if (currentStatus != OrderStatus.PENDING) {
                throw new BadRequestException("Đơn hàng ở trạng thái '" + currentStatus + "' không thể tự hủy.");
            }
        }

        // Kiểm tra logic luồng chuyển đổi trạng thái hợp lệ
        validateStatusTransition(currentStatus, newStatus);

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Cập nhật trạng thái đơn hàng id={} từ {} sang {}", id, currentStatus, newStatus);

        return mapOrderWithSerials(updated);
    }

    @Transactional
    public void deleteOrder(Long id, boolean isStaffOrAdmin) {
        if (!isStaffOrAdmin) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền xóa đơn hàng.");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Không thể xóa đơn hàng đã hoàn tất giao dịch.");
        }

        if (orderRepository.countInvoices(id) > 0) {
            throw new BadRequestException("Không thể xóa đơn hàng đã phát sinh hóa đơn bán hàng.");
        }

        if (orderRepository.countPayments(id) > 0) {
            throw new BadRequestException("Không thể xóa đơn hàng đã có bản ghi thanh toán.");
        }

        orderRepository.delete(order);
        log.info("Đã xóa vĩnh viễn đơn hàng id={}, code={}", id, order.getCode());
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        // PENDING -> CONFIRMED, REJECTED, CANCELLED
        if (from == OrderStatus.PENDING) {
            if (to == OrderStatus.CONFIRMED || to == OrderStatus.REJECTED || to == OrderStatus.CANCELLED) {
                return;
            }
        }
        // CONFIRMED -> PROCESSING, CANCELLED
        if (from == OrderStatus.CONFIRMED) {
            if (to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED) {
                return;
            }
        }
        // PROCESSING -> COMPLETED, CANCELLED, RETURNED
        if (from == OrderStatus.PROCESSING) {
            if (to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED || to == OrderStatus.RETURNED) {
                return;
            }
        }
        // COMPLETED -> RETURNED
        if (from == OrderStatus.COMPLETED) {
            if (to == OrderStatus.RETURNED) {
                return;
            }
        }

        // Cho phép chuyển đổi nếu cần xử lý đặc biệt bởi nhân viên quản trị
        log.warn("Chuyển trạng thái đơn hàng không theo chuỗi tiêu chuẩn: {} -> {}", from, to);
    }

    private OrderResponse mapOrderWithSerials(Order order) {
        List<OrderItemSerial> serials = orderItemSerialRepository.findAllByOrderId(order.getId());
        Map<Long, List<String>> itemSerialMap = new HashMap<>();

        for (OrderItemSerial ois : serials) {
            ProductSerial ps = ois.getProductSerial();
            if (ps != null && ps.getSerialNumber() != null) {
                itemSerialMap.computeIfAbsent(ois.getOrderItemId(), k -> new ArrayList<>())
                        .add(ps.getSerialNumber());
            }
        }

        return orderMapper.toResponse(order, itemSerialMap);
    }

    private String generateUniqueOrderCode() {
        for (int i = 0; i < 10; i++) {
            long ts = System.currentTimeMillis() % 100000000;
            int rand = (int) (Math.random() * 900 + 100);
            String candidate = "DH-" + ts + rand;
            if (!orderRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        return "DH-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    private void validateDateRange(Instant fromDate, Instant toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("Thời điểm bắt đầu không được lớn hơn thời điểm kết thúc.");
        }
    }
}

