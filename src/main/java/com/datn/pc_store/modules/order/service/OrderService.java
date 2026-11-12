package com.datn.pc_store.modules.order.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.DuplicateResourceException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.service.DeliveryService;
import com.datn.pc_store.modules.goodsreceipt.entity.ProductSerial;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import com.datn.pc_store.modules.goodsreceipt.repository.ProductSerialRepository;
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
    private final DeliveryService deliveryService;
    private final OrderItemSerialRepository orderItemSerialRepository;
    private final ProductSerialRepository productSerialRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderSerialService orderSerialService;
    private final com.datn.pc_store.modules.warranty.service.WarrantyActivationService warrantyActivationService;

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
        orderSerialService.reserve(saved, req.getItems());
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

        // Quyền khách hàng: chỉ được hủy khi đơn còn PENDING hoặc xác nhận khi đơn đang giao
        if (!isStaffOrAdmin) {
            if (!order.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("Bạn không có quyền cập nhật trạng thái đơn hàng này.");
            }
            if (newStatus == OrderStatus.CANCELLED) {
                if (currentStatus != OrderStatus.PENDING) {
                    throw new BadRequestException("Đơn hàng ở trạng thái '" + currentStatus + "' không thể tự hủy.");
                }
            } else if (newStatus == OrderStatus.COMPLETED) {
                if (currentStatus != OrderStatus.PROCESSING) {
                    throw new BadRequestException("Chỉ có thể xác nhận nhận hàng khi đơn đang trong quá trình xử lý/vận chuyển.");
                }
            } else {
                throw new BadRequestException("Khách hàng chỉ được phép hủy đơn chờ duyệt hoặc xác nhận nhận hàng.");
            }
        }

        if (newStatus == OrderStatus.COMPLETED
                && (currentStatus == OrderStatus.PENDING || currentStatus == OrderStatus.CONFIRMED
                    || currentStatus == OrderStatus.PROCESSING)) {
            if (deliveryService.existsForResource(DeliveryResourceType.ORDER, id)) {
                throw new BadRequestException("Đơn đã có vận đơn; hãy cập nhật vận đơn sang DELIVERED để hoàn tất.");
            }
            orderSerialService.markSold(id);
            order.setStatus(OrderStatus.COMPLETED);
            warrantyActivationService.activateForCompletedOrder(order);
            return mapOrderWithSerials(orderRepository.save(order));
        }

        if (currentStatus == OrderStatus.PROCESSING
                && (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED)) {
            throw new BadRequestException("Đơn đã bắt đầu giao; hãy cập nhật trạng thái vận đơn để hoàn tất hoặc hủy giao.");
        }

        // Kiểm tra logic luồng chuyển đổi trạng thái hợp lệ
        validateStatusTransition(currentStatus, newStatus);

        if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REJECTED) {
            orderSerialService.release(id);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Cập nhật trạng thái đơn hàng id={} từ {} sang {}", id, currentStatus, newStatus);

        // ĐẶC BIỆT: CẬP NHẬT TRẠNG THÁI SERIAL THEO QUY TRÌNH BÁN HÀNG
        handleSerialStatusTransition(id, newStatus);

        return mapOrderWithSerials(updated);
    }

    /**
     * Giai đoạn 3: Soạn hàng, đóng gói và gán mã định danh (Serial) vào đơn hàng
     * Trạng thái serial chuyển sang PACKING (Đang đóng gói)
     */
    @Transactional
    public OrderResponse assignSerialsToOrder(Long orderId, Map<Long, List<String>> itemSerials) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", orderId));

        if (itemSerials == null || itemSerials.isEmpty()) {
            throw new BadRequestException("Danh sách serial quét gán không được để trống.");
        }

        // Tìm tất cả order items thuộc đơn hàng này
        Map<Long, OrderItem> itemMap = order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));

        for (Map.Entry<Long, List<String>> entry : itemSerials.entrySet()) {
            Long orderItemId = entry.getKey();
            List<String> serialNumbers = entry.getValue();

            OrderItem item = itemMap.get(orderItemId);
            if (item == null) {
                throw new BadRequestException("Mặt hàng id=" + orderItemId + " không thuộc đơn hàng " + order.getCode());
            }

            if (serialNumbers == null || serialNumbers.isEmpty()) {
                continue;
            }

            // Xóa các liên kết serial cũ của item này (nếu có để quét lại)
            List<OrderItemSerial> existingOis = orderItemSerialRepository.findAllByOrderItemId(orderItemId);
            if (!existingOis.isEmpty()) {
                orderItemSerialRepository.deleteAll(existingOis);
            }

            // Tìm và kiểm tra các serial trong database
            List<ProductSerial> serials = productSerialRepository.findAllBySerialNumberIn(serialNumbers);
            if (serials.size() != serialNumbers.size()) {
                Set<String> found = serials.stream().map(ProductSerial::getSerialNumber).collect(Collectors.toSet());
                List<String> missing = serialNumbers.stream().filter(s -> !found.contains(s)).toList();
                throw new BadRequestException("Các mã serial không tồn tại trong hệ thống: " + String.join(", ", missing));
            }

            for (ProductSerial ps : serials) {
                if (!ps.getProductId().equals(item.getProductId())) {
                    throw new BadRequestException("Serial '" + ps.getSerialNumber() + "' không thuộc sản phẩm ID: " + item.getProductId());
                }

                // Gán liên kết OrderItemSerial
                OrderItemSerial ois = OrderItemSerial.builder()
                        .orderItemId(orderItemId)
                        .productSerialId(ps.getId())
                        .build();
                orderItemSerialRepository.save(ois);

                // Cập nhật trạng thái serial thành PACKING (6 - Đang đóng gói)
                ps.setStatus(ProductSerialStatus.PACKING);
            }
            productSerialRepository.saveAll(serials);
        }

        // Cập nhật trạng thái đơn hàng sang PROCESSING (Đang đóng gói/xử lý) nếu đang ở CONFIRMED
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }

        log.info("Đã quét và gán serial thành công cho đơn hàng id={}, chuyển trạng thái serial sang PACKING", orderId);
        return mapOrderWithSerials(order);
    }

    /**
     * Giai đoạn 3 Bước 5: Bàn giao cho ĐVVC
     * Cập nhật trạng thái các mã serial thành DELIVERING (7 - Đang giao)
     */
    @Transactional
    public OrderResponse handoverToCarrier(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", orderId));

        List<OrderItemSerial> serialLinks = orderItemSerialRepository.findAllByOrderId(orderId);
        if (!serialLinks.isEmpty()) {
            List<Long> serialIds = serialLinks.stream().map(OrderItemSerial::getProductSerialId).toList();
            List<ProductSerial> serials = productSerialRepository.findAllByIdIn(serialIds);
            for (ProductSerial ps : serials) {
                ps.setStatus(ProductSerialStatus.DELIVERING);
            }
            productSerialRepository.saveAll(serials);
            log.info("Đơn hàng id={} đã bàn giao ĐVVC, chuyển trạng thái {} serials sang DELIVERING", orderId, serials.size());
        }

        return mapOrderWithSerials(order);
    }

    /**
     * Giai đoạn 4: Hoàn thành đơn hàng & cập nhật trạng thái serial sang SOLD (Đã bán)
     */
    private void handleSerialStatusTransition(Long orderId, OrderStatus newStatus) {
        List<OrderItemSerial> serialLinks = orderItemSerialRepository.findAllByOrderId(orderId);
        if (serialLinks.isEmpty()) {
            return;
        }

        List<Long> serialIds = serialLinks.stream().map(OrderItemSerial::getProductSerialId).toList();
        List<ProductSerial> serials = productSerialRepository.findAllByIdIn(serialIds);

        if (newStatus == OrderStatus.COMPLETED) {
            // Cập nhật lại trạng thái serial sau khi đã hoàn thành đơn hàng -> SOLD (8)
            for (ProductSerial ps : serials) {
                ps.setStatus(ProductSerialStatus.SOLD);
            }
            productSerialRepository.saveAll(serials);
            log.info("QUY TRÌNH BÁN HÀNG HOÀN TẤT: Đã cập nhật {} serials của đơn hàng id={} sang trạng thái SOLD (Đã bán)",
                    serials.size(), orderId);
        } else if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REJECTED || newStatus == OrderStatus.RETURNED) {
            // Trả lại kho nếu hủy hoặc hoàn đơn
            for (ProductSerial ps : serials) {
                ps.setStatus(ProductSerialStatus.IN_STOCK);
            }
            productSerialRepository.saveAll(serials);
            log.info("Đơn hàng id={} hủy/hoàn: Đã hoàn trả {} serials về trạng thái IN_STOCK (Trong kho)",
                    serials.size(), orderId);
        }
    }

    @Transactional
    public void deleteOrder(Long id, boolean isStaffOrAdmin) {
        if (!isStaffOrAdmin) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền xóa đơn hàng.");
        }

        Order order = orderRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));

        if (deliveryService.existsForResource(DeliveryResourceType.ORDER, id)) {
            throw new BadRequestException("Không thể xóa đơn hàng đã có vận đơn.");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Không thể xóa đơn hàng đã hoàn tất giao dịch.");
        }

        if (orderRepository.countInvoices(id) > 0) {
            throw new BadRequestException("Không thể xóa đơn hàng đã phát sinh hóa đơn bán hàng.");
        }

        if (orderRepository.countPayments(id) > 0) {
            throw new BadRequestException("Không thể xóa đơn hàng đã có bản ghi thanh toán.");
        }

        // Hoàn trả serial nếu có
        List<OrderItemSerial> serialLinks = orderItemSerialRepository.findAllByOrderId(id);
        if (!serialLinks.isEmpty()) {
            List<Long> serialIds = serialLinks.stream().map(OrderItemSerial::getProductSerialId).toList();
            List<ProductSerial> serials = productSerialRepository.findAllByIdIn(serialIds);
            for (ProductSerial ps : serials) {
                ps.setStatus(ProductSerialStatus.IN_STOCK);
            }
            productSerialRepository.saveAll(serials);
            orderItemSerialRepository.deleteAll(serialLinks);
        }

        orderRepository.delete(order);
        log.info("Đã xóa vĩnh viễn đơn hàng id={}, code={}", id, order.getCode());
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == OrderStatus.PENDING) {
            if (to == OrderStatus.CONFIRMED || to == OrderStatus.REJECTED || to == OrderStatus.CANCELLED) {
                return;
            }
        }
        if (from == OrderStatus.CONFIRMED) {
            if (to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED) {
                return;
            }
        }
        if (from == OrderStatus.PROCESSING) {
            if (to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED || to == OrderStatus.RETURNED) {
                return;
            }
        }
        if (from == OrderStatus.COMPLETED) {
            if (to == OrderStatus.RETURNED) {
                return;
            }
        }

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
