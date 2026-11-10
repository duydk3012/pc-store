package com.datn.pc_store.modules.purchasereturn.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.DuplicateResourceException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.goodsreceipt.entity.GoodsReceipt;
import com.datn.pc_store.modules.goodsreceipt.entity.ProductSerial;
import com.datn.pc_store.modules.goodsreceipt.entity.ReceiptItem;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import com.datn.pc_store.modules.goodsreceipt.repository.ProductSerialRepository;
import com.datn.pc_store.modules.product.entity.Product;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import com.datn.pc_store.modules.purchaseorder.entity.PurchaseOrder;
import com.datn.pc_store.modules.purchaseorder.repository.PurchaseOrderRepository;
import com.datn.pc_store.modules.purchasereturn.dto.*;
import com.datn.pc_store.modules.purchasereturn.entity.PurchaseReturn;
import com.datn.pc_store.modules.purchasereturn.entity.PurchaseReturnItem;
import com.datn.pc_store.modules.purchasereturn.enums.PurchaseReturnStatus;
import com.datn.pc_store.modules.purchasereturn.mapper.PurchaseReturnMapper;
import com.datn.pc_store.modules.purchasereturn.repository.PurchaseReturnItemRepository;
import com.datn.pc_store.modules.purchasereturn.repository.PurchaseReturnRepository;
import com.datn.pc_store.modules.suppliers.entity.Supplier;
import com.datn.pc_store.modules.suppliers.repository.SupplierRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseReturnService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "code", "name", "supplierId", "returnDate", "status", "totalAmount",
            "createdDate", "createdBy", "lastModifiedDate", "lastModifiedBy"
    );

    private static final Set<ProductSerialStatus> ELIGIBLE_SERIAL_STATUSES = Set.of(
            ProductSerialStatus.DEFECTIVE,
            ProductSerialStatus.PENDING_RETURN,
            ProductSerialStatus.IN_STOCK
    );

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseReturnItemRepository purchaseReturnItemRepository;
    private final ProductSerialRepository productSerialRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseReturnMapper purchaseReturnMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public PageResponse<PurchaseReturnResponse> getAllReturns(
            String keyword,
            PurchaseReturnStatus status,
            Long supplierId,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    ) {
        validateDates(fromDate, toDate);
        for (var sort : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(sort.getProperty())) {
                throw new BadRequestException("Trường sắp xếp không hợp lệ: " + sort.getProperty()
                        + ". Sử dụng sort=createdDate,desc hoặc bỏ tham số sort.");
            }
        }

        boolean isAdmin = hasRole("ADMIN");
        boolean isManager = hasRole("MANAGER");
        boolean isWarehouse = hasRole("STAFF_WAREHOUSE");

        Long currentUserId = getCurrentUserId();
        int filterMode = 0; // 0: ADMIN (all)
        Collection<PurchaseReturnStatus> managerStatuses = List.of(
                PurchaseReturnStatus.PENDING,
                PurchaseReturnStatus.DELIVERING,
                PurchaseReturnStatus.RETURNED
        );
        PurchaseReturnStatus draftStatus = PurchaseReturnStatus.DRAFT;
        PurchaseReturnStatus cancelledStatus = PurchaseReturnStatus.CANCELLED;

        if (isAdmin) {
            filterMode = 0;
        } else if (isManager) {
            if (currentUserId == null && (status == PurchaseReturnStatus.CANCELLED || status == PurchaseReturnStatus.DRAFT)) {
                return PageResponse.of(Page.<PurchaseReturn>empty(pageable).map(purchaseReturnMapper::toResponse));
            }
            filterMode = 2;
        } else if (isWarehouse) {
            if (currentUserId == null) {
                return PageResponse.of(Page.<PurchaseReturn>empty(pageable).map(purchaseReturnMapper::toResponse));
            }
            filterMode = 1;
        }

        Page<PurchaseReturn> page = purchaseReturnRepository.searchReturns(
                keyword, status, supplierId, filterMode, currentUserId,
                managerStatuses, draftStatus, cancelledStatus, fromDate, toDate, pageable
        );

        return PageResponse.of(page.map(this::toEnrichedResponse));
    }

    @Transactional(readOnly = true)
    public PurchaseReturnResponse getReturnById(Long id) {
        PurchaseReturn pr = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu hoàn trả", "id", id));
        validateReadPermission(pr);
        return toEnrichedResponse(pr);
    }

    @Transactional(readOnly = true)
    public PurchaseReturnResponse getReturnByCode(String code) {
        PurchaseReturn pr = purchaseReturnRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu hoàn trả", "code", code));
        validateReadPermission(pr);
        return toEnrichedResponse(pr);
    }

    @Transactional
    public PurchaseReturnResponse createReturn(PurchaseReturnCreateRequest req) {
        String code = req.getCode() != null ? req.getCode().trim() : "";
        validateCode(code, null);

        Supplier supplier = validateAndGetSupplier(req.getSupplierId());

        PurchaseReturnStatus targetStatus = req.getStatus() == null ? PurchaseReturnStatus.PENDING : req.getStatus();
        if (targetStatus != PurchaseReturnStatus.PENDING && targetStatus != PurchaseReturnStatus.DRAFT) {
            throw new BadRequestException("Khi khởi tạo phiếu hoàn trả, trạng thái chỉ có thể là PENDING hoặc DRAFT.");
        }

        Long currentUserId = getCurrentUserId();

        PurchaseReturn pr = PurchaseReturn.builder()
                .code(code)
                .name(req.getName())
                .supplierId(supplier.getId())
                .reason(req.getReason())
                .description(req.getDescription())
                .returnDate(req.getReturnDate())
                .status(targetStatus)
                .build();

        if (currentUserId != null) {
            pr.setCreatedBy(currentUserId);
        }

        List<ProductSerial> serials = resolveAndValidateSerials(
                req.getSerialNumbers(),
                req.getProductSerialIds(),
                supplier.getId(),
                null
        );

        if (targetStatus == PurchaseReturnStatus.PENDING && serials.isEmpty()) {
            throw new BadRequestException("Phiếu hoàn trả ở trạng thái PENDING phải có ít nhất một serial sản phẩm.");
        }

        List<PurchaseReturnItem> items = new ArrayList<>();
        long totalAmount = 0L;

        for (ProductSerial serial : serials) {
            long unitCost = getSerialUnitCost(serial);
            totalAmount += unitCost;

            PurchaseReturnItem item = PurchaseReturnItem.builder()
                    .purchaseReturn(pr)
                    .productSerial(serial)
                    .unitCost(unitCost)
                    .build();
            if (currentUserId != null) {
                item.setCreatedBy(currentUserId);
            }
            items.add(item);

            if (targetStatus == PurchaseReturnStatus.PENDING) {
                serial.setStatus(ProductSerialStatus.PENDING_RETURN);
            }
        }

        pr.setTotalAmount(totalAmount);
        pr.setItems(items);

        PurchaseReturn saved = purchaseReturnRepository.saveAndFlush(pr);
        log.info("Đã tạo phiếu hoàn trả NCC id: {} với trạng thái: {}", saved.getId(), saved.getStatus());
        return toEnrichedResponse(saved);
    }

    @Transactional
    public PurchaseReturnResponse updateReturn(Long id, PurchaseReturnUpdateRequest req) {
        PurchaseReturn pr = lockReturn(id);
        requireEditable(pr);

        Long currentUserId = getCurrentUserId();
        if (pr.getCreatedBy() != null && currentUserId != null
                && !currentUserId.equals(pr.getCreatedBy()) && !hasRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ người tạo mới có thể chỉnh sửa phiếu hoàn trả nháp.");
        }

        String code = req.getCode() != null ? req.getCode().trim() : "";
        validateCode(code, id);

        List<ProductSerial> serials = resolveAndValidateSerials(
                req.getSerialNumbers(),
                req.getProductSerialIds(),
                pr.getSupplierId(),
                id
        );

        pr.setCode(code);
        pr.setName(req.getName());
        pr.setReason(req.getReason());
        pr.setDescription(req.getDescription());
        pr.setReturnDate(req.getReturnDate());

        pr.getItems().clear();
        entityManager.flush();

        long totalAmount = 0L;
        List<PurchaseReturnItem> items = new ArrayList<>();
        for (ProductSerial serial : serials) {
            long unitCost = getSerialUnitCost(serial);
            totalAmount += unitCost;

            PurchaseReturnItem item = PurchaseReturnItem.builder()
                    .purchaseReturn(pr)
                    .productSerial(serial)
                    .unitCost(unitCost)
                    .build();
            if (currentUserId != null) {
                item.setCreatedBy(currentUserId);
            }
            items.add(item);
        }

        pr.setTotalAmount(totalAmount);
        pr.getItems().addAll(items);

        PurchaseReturn saved = purchaseReturnRepository.saveAndFlush(pr);
        log.info("Đã cập nhật phiếu hoàn trả nháp id: {}", id);
        return toEnrichedResponse(saved);
    }

    @Transactional
    public PurchaseReturnResponse updateStatus(Long id, PurchaseReturnStatusUpdateRequest req) {
        PurchaseReturn pr = lockReturn(id);
        PurchaseReturnStatus current = pr.getStatus();
        PurchaseReturnStatus target = req.getStatus();

        if (current == target) {
            return toEnrichedResponse(pr);
        }

        boolean isAdmin = hasRole("ADMIN");
        boolean isManager = hasRole("MANAGER");
        boolean isWarehouse = hasRole("STAFF_WAREHOUSE");
        Long currentUserId = getCurrentUserId();
        Long createdBy = pr.getCreatedBy();

        if (current == PurchaseReturnStatus.CANCELLED) {
            throw conflict("Phiếu hoàn trả đã bị hủy, không thể thay đổi trạng thái.");
        }
        if (current == PurchaseReturnStatus.RETURNED) {
            throw conflict("Phiếu hoàn trả đã hoàn tất (RETURNED), không thể thay đổi trạng thái.");
        }
        if (current == PurchaseReturnStatus.DELIVERING
                && (target == PurchaseReturnStatus.RETURNED || target == PurchaseReturnStatus.CANCELLED)) {
            throw conflict("Phiếu đang giao; hãy cập nhật trạng thái vận đơn để ghi nhận kết quả giao hàng.");
        }

        boolean allowed = switch (current) {
            case DRAFT -> target == PurchaseReturnStatus.PENDING || target == PurchaseReturnStatus.CANCELLED;
            case PENDING -> target == PurchaseReturnStatus.DELIVERING || target == PurchaseReturnStatus.CANCELLED;
            case DELIVERING -> target == PurchaseReturnStatus.RETURNED || target == PurchaseReturnStatus.CANCELLED;
            case RETURNED, CANCELLED -> false;
        };

        if (!allowed) {
            throw conflict("Không thể chuyển phiếu hoàn trả từ trạng thái " + current + " sang " + target + ".");
        }

        switch (target) {
            case PENDING -> {
                if (createdBy != null && currentUserId != null && !currentUserId.equals(createdBy) && !isAdmin) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ người tạo hoặc Quản trị viên mới có quyền gửi duyệt phiếu hoàn trả.");
                }
                if (pr.getItems().isEmpty()) {
                    throw new BadRequestException("Phiếu hoàn trả phải có ít nhất một serial sản phẩm khi chuyển sang PENDING.");
                }
                for (PurchaseReturnItem item : pr.getItems()) {
                    ProductSerial ps = item.getProductSerial();
                    if (ps != null) {
                        ps.setStatus(ProductSerialStatus.PENDING_RETURN);
                    }
                }
            }
            case DELIVERING -> {
                if (!isAdmin && !isManager && !isWarehouse) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền chuyển trạng thái giao hàng.");
                }
                pr.startDelivery();
            }
            case RETURNED -> {
                if (!isAdmin && !isManager) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ Quản lý (MANAGER) hoặc Quản trị viên (ADMIN) mới có quyền xác nhận NCC đã nhận hàng hoàn.");
                }
                for (PurchaseReturnItem item : pr.getItems()) {
                    ProductSerial ps = item.getProductSerial();
                    if (ps != null) {
                        ps.setStatus(ProductSerialStatus.RETURNED);
                        ps.setTerminated(true);
                    }
                }
            }
            case CANCELLED -> {
                if (current == PurchaseReturnStatus.DRAFT && createdBy != null && currentUserId != null
                        && !currentUserId.equals(createdBy) && !isAdmin) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ người tạo mới có quyền hủy phiếu hoàn trả nháp.");
                }
                for (PurchaseReturnItem item : pr.getItems()) {
                    ProductSerial ps = item.getProductSerial();
                    if (ps != null) {
                        ps.setStatus(ProductSerialStatus.DEFECTIVE);
                        ps.setTerminated(false);
                    }
                }
            }
        }

        pr.setStatus(target);
        if (req.getNote() != null && !req.getNote().isBlank()) {
            String currentDesc = pr.getDescription() == null ? "" : pr.getDescription() + " | ";
            pr.setDescription(currentDesc + "Ghi chú chuyển trạng thái: " + req.getNote().trim());
        }

        PurchaseReturn saved = purchaseReturnRepository.saveAndFlush(pr);
        log.info("Phiếu hoàn trả id: {} đã chuyển từ trạng thái {} sang {}", id, current, target);
        return toEnrichedResponse(saved);
    }

    @Transactional
    public void deleteReturn(Long id) {
        PurchaseReturn pr = lockReturn(id);
        if (pr.getStatus() != PurchaseReturnStatus.DRAFT) {
            throw conflict("Chỉ có thể xóa phiếu hoàn trả khi ở trạng thái nháp (DRAFT).");
        }
        Long currentUserId = getCurrentUserId();
        if (pr.getCreatedBy() != null && currentUserId != null
                && !currentUserId.equals(pr.getCreatedBy()) && !hasRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ người tạo hoặc Quản trị viên mới có quyền xóa phiếu nháp.");
        }
        purchaseReturnRepository.delete(pr);
        log.info("Đã xóa phiếu hoàn trả nháp id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<AvailableSerialResponse> getAvailableSerialsForReturn(Long supplierId, Long productId, String keyword) {
        validateAndGetSupplier(supplierId);
        List<ProductSerial> serials = purchaseReturnRepository.findAvailableSerialsForReturn(
                supplierId,
                ELIGIBLE_SERIAL_STATUSES,
                List.of(PurchaseReturnStatus.CANCELLED),
                null,
                productId,
                keyword
        );

        return enrichAvailableSerials(serials);
    }

    @Transactional(readOnly = true)
    public AvailableSerialResponse validateSerialForReturn(Long supplierId, String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isBlank()) {
            throw new BadRequestException("Mã serial không được để trống.");
        }
        String cleanSn = serialNumber.trim();
        validateAndGetSupplier(supplierId);

        List<ProductSerial> matched = purchaseReturnRepository.findActiveSerialsWithReceiptBySerialNumber(cleanSn);
        if (matched.isEmpty()) {
            throw new ResourceNotFoundException("Serial sản phẩm", "serialNumber", cleanSn);
        }

        ProductSerial ps = matched.get(0);
        if (ps.isTerminated()) {
            throw new BadRequestException("Serial '" + cleanSn + "' đã bị kết thúc/hủy, không thể xuất hoàn trả.");
        }

        ReceiptItem ri = ps.getReceiptItem();
        if (ri == null || ri.getGoodsReceipt() == null) {
            throw new BadRequestException("Serial '" + cleanSn + "' không có liên kết phiếu nhập kho hợp lệ.");
        }

        GoodsReceipt gr = ri.getGoodsReceipt();
        PurchaseOrder po = purchaseOrderRepository.findById(gr.getPurchaseOrderId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn nhập hàng gốc của serial '" + cleanSn + "'."));

        if (!po.getSupplierId().equals(supplierId)) {
            Supplier actualSup = supplierRepository.findById(po.getSupplierId()).orElse(null);
            String actualSupName = actualSup != null ? actualSup.getName() : "ID " + po.getSupplierId();
            throw new BadRequestException("Serial '" + cleanSn + "' thuộc Nhà cung cấp '" + actualSupName
                    + "', không khớp với Nhà cung cấp đang chọn.");
        }

        if (!ELIGIBLE_SERIAL_STATUSES.contains(ps.getStatus())) {
            throw new BadRequestException("Serial '" + cleanSn + "' đang có trạng thái " + ps.getStatus()
                    + ", không đủ điều kiện xuất hoàn trả.");
        }

        Set<Long> inUse = purchaseReturnItemRepository.findSerialIdsInActiveReturns(
                List.of(ps.getId()),
                List.of(PurchaseReturnStatus.CANCELLED),
                null
        );
        if (!inUse.isEmpty()) {
            throw new DuplicateResourceException("Serial '" + cleanSn + "' hiện đang nằm trong một phiếu hoàn trả khác đang xử lý.");
        }

        return enrichAvailableSerials(List.of(ps)).get(0);
    }

    private List<ProductSerial> resolveAndValidateSerials(
            List<String> serialNumbers,
            List<Long> productSerialIds,
            Long supplierId,
            Long excludeReturnId
    ) {
        Set<String> cleanSns = serialNumbers == null ? Collections.emptySet() :
                serialNumbers.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> cleanIds = productSerialIds == null ? Collections.emptySet() :
                productSerialIds.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (cleanSns.isEmpty() && cleanIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ProductSerial> serialMap = new LinkedHashMap<>();

        if (!cleanIds.isEmpty()) {
            List<ProductSerial> byIds = productSerialRepository.findAllById(cleanIds);
            for (ProductSerial ps : byIds) {
                serialMap.put(ps.getId(), ps);
            }
            if (byIds.size() < cleanIds.size()) {
                Set<Long> foundIds = byIds.stream().map(ProductSerial::getId).collect(Collectors.toSet());
                List<Long> missing = cleanIds.stream().filter(id -> !foundIds.contains(id)).toList();
                throw new ResourceNotFoundException("Serial sản phẩm", "ids", missing);
            }
        }

        if (!cleanSns.isEmpty()) {
            for (String sn : cleanSns) {
                List<ProductSerial> list = purchaseReturnRepository.findActiveSerialsWithReceiptBySerialNumber(sn);
                if (list.isEmpty()) {
                    throw new ResourceNotFoundException("Serial sản phẩm", "serialNumber", sn);
                }
                ProductSerial ps = list.get(0);
                serialMap.put(ps.getId(), ps);
            }
        }

        List<ProductSerial> serialList = new ArrayList<>(serialMap.values());
        List<Long> allSerialIds = serialList.stream().map(ProductSerial::getId).toList();

        Set<Long> alreadyInActive = purchaseReturnItemRepository.findSerialIdsInActiveReturns(
                allSerialIds,
                List.of(PurchaseReturnStatus.CANCELLED),
                excludeReturnId
        );

        for (ProductSerial ps : serialList) {
            if (ps.isTerminated()) {
                throw new BadRequestException("Serial '" + ps.getSerialNumber() + "' đã bị kết thúc, không thể hoàn trả.");
            }

            if (!ELIGIBLE_SERIAL_STATUSES.contains(ps.getStatus())) {
                throw new BadRequestException("Serial '" + ps.getSerialNumber() + "' có trạng thái " + ps.getStatus()
                        + " không đủ điều kiện xuất hoàn trả.");
            }

            if (alreadyInActive.contains(ps.getId())) {
                throw new DuplicateResourceException("Serial '" + ps.getSerialNumber() + "' hiện đang nằm trong một phiếu hoàn trả khác đang xử lý.");
            }

            ReceiptItem ri = ps.getReceiptItem();
            if (ri == null || ri.getGoodsReceipt() == null) {
                throw new BadRequestException("Serial '" + ps.getSerialNumber() + "' không có phiếu nhập gốc.");
            }

            GoodsReceipt gr = ri.getGoodsReceipt();
            PurchaseOrder po = purchaseOrderRepository.findById(gr.getPurchaseOrderId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn nhập hàng gốc của serial '" + ps.getSerialNumber() + "'."));

            if (!po.getSupplierId().equals(supplierId)) {
                Supplier actualSup = supplierRepository.findById(po.getSupplierId()).orElse(null);
                String actualName = actualSup != null ? actualSup.getName() : "ID " + po.getSupplierId();
                throw new BadRequestException("Serial '" + ps.getSerialNumber() + "' thuộc Nhà cung cấp '" + actualName
                        + "', không khớp với Nhà cung cấp của phiếu hoàn.");
            }
        }

        return serialList;
    }

    private List<AvailableSerialResponse> enrichAvailableSerials(List<ProductSerial> serials) {
        if (serials == null || serials.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = serials.stream().map(ProductSerial::getProductId).distinct().toList();
        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return serials.stream().map(ps -> {
            Product prod = productMap.get(ps.getProductId());
            ReceiptItem ri = ps.getReceiptItem();
            GoodsReceipt gr = ri != null ? ri.getGoodsReceipt() : null;

            return AvailableSerialResponse.builder()
                    .productSerialId(ps.getId())
                    .serialNumber(ps.getSerialNumber())
                    .productId(ps.getProductId())
                    .productCode(prod != null ? prod.getCode() : null)
                    .productName(prod != null ? prod.getName() : null)
                    .status(ps.getStatus())
                    .unitCost(ri != null ? ri.getActualUnitCost() : 0L)
                    .receiptCode(gr != null ? gr.getCode() : null)
                    .receiptDate(gr != null ? gr.getReceivedAt() : null)
                    .build();
        }).toList();
    }

    private PurchaseReturnResponse toEnrichedResponse(PurchaseReturn pr) {
        PurchaseReturnResponse resp = purchaseReturnMapper.toResponse(pr);
        if (resp == null) return null;

        supplierRepository.findById(pr.getSupplierId()).ifPresent(sup -> {
            resp.setSupplierCode(sup.getCode());
            resp.setSupplierName(sup.getName());
        });

        if (pr.getItems() != null && !pr.getItems().isEmpty()) {
            List<Long> productIds = pr.getItems().stream()
                    .map(PurchaseReturnItem::getProductSerial)
                    .filter(Objects::nonNull)
                    .map(ProductSerial::getProductId)
                    .distinct()
                    .toList();

            Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            List<PurchaseReturnItemResponse> itemResponses = pr.getItems().stream().map(item -> {
                PurchaseReturnItemResponse itemResp = purchaseReturnMapper.toItemResponse(item);
                ProductSerial ps = item.getProductSerial();
                if (ps != null) {
                    Product prod = productMap.get(ps.getProductId());
                    if (prod != null) {
                        itemResp.setProductCode(prod.getCode());
                        itemResp.setProductName(prod.getName());
                    }
                    ReceiptItem ri = ps.getReceiptItem();
                    if (ri != null && ri.getGoodsReceipt() != null) {
                        itemResp.setReceiptCode(ri.getGoodsReceipt().getCode());
                        itemResp.setReceiptDate(ri.getGoodsReceipt().getReceivedAt());
                    }
                }
                return itemResp;
            }).toList();

            resp.setItems(itemResponses);
        }

        return resp;
    }

    private long getSerialUnitCost(ProductSerial serial) {
        if (serial.getReceiptItem() != null) {
            return serial.getReceiptItem().getActualUnitCost();
        }
        return 0L;
    }

    private Supplier validateAndGetSupplier(Long supplierId) {
        if (supplierId == null) {
            throw new BadRequestException("ID nhà cung cấp không được để trống.");
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", "id", supplierId));
    }

    private void validateCode(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Mã phiếu hoàn trả không được để trống.");
        }
        if (purchaseReturnRepository.countByCode(code, excludeId) > 0) {
            throw new DuplicateResourceException("Mã phiếu hoàn trả đã tồn tại: " + code);
        }
    }

    private PurchaseReturn lockReturn(Long id) {
        return purchaseReturnRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu hoàn trả", "id", id));
    }

    private void requireEditable(PurchaseReturn pr) {
        if (pr.getStatus() != PurchaseReturnStatus.DRAFT) {
            throw conflict("Chỉ có thể chỉnh sửa phiếu hoàn trả ở trạng thái nháp (DRAFT). Trạng thái hiện tại: " + pr.getStatus());
        }
    }

    private void validateReadPermission(PurchaseReturn pr) {
        boolean isAdmin = hasRole("ADMIN");
        boolean isManager = hasRole("MANAGER");
        boolean isWarehouse = hasRole("STAFF_WAREHOUSE");
        Long currentUserId = getCurrentUserId();

        if (isAdmin) return;

        if (isWarehouse) {
            if (pr.getCreatedBy() == null || currentUserId == null || !currentUserId.equals(pr.getCreatedBy())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ có thể xem phiếu hoàn trả do chính mình tạo.");
            }
            return;
        }

        if (isManager) {
            if (pr.getStatus() == PurchaseReturnStatus.DRAFT || pr.getStatus() == PurchaseReturnStatus.CANCELLED) {
                if (pr.getCreatedBy() == null || currentUserId == null || !currentUserId.equals(pr.getCreatedBy())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Quản lý chỉ xem được phiếu nháp hoặc phiếu đã hủy do chính mình tạo.");
                }
            }
        }
    }

    private void validateDates(Instant fromDate, Instant toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("Thời gian bắt đầu (fromDate) phải trước hoặc bằng thời gian kết thúc (toDate).");
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    private boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        String target = "ROLE_" + roleName;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(target));
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
