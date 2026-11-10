package com.datn.pc_store.modules.delivery.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.DuplicateResourceException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.delivery.dto.DeliveryMetadataUpdateRequest;
import com.datn.pc_store.modules.delivery.dto.DeliveryCreateRequest;
import com.datn.pc_store.modules.delivery.dto.DeliveryResponse;
import com.datn.pc_store.modules.delivery.entity.Delivery;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import com.datn.pc_store.modules.delivery.handler.DeliveryResourceHandler;
import com.datn.pc_store.modules.delivery.handler.DeliveryResourceHandlerFactory;
import com.datn.pc_store.modules.delivery.repository.DeliveryRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private static final List<String> SORTABLE_FIELDS = List.of(
            "id", "resourceType", "resourceId", "trackingCode", "carrierName", "shippingFee", "status",
            "createdDate", "lastModifiedDate"
    );

    private final DeliveryRepository deliveryRepository;
    private final DeliveryResourceHandlerFactory handlerFactory;

    @Transactional
    public DeliveryResponse create(DeliveryCreateRequest request, Long currentUserId) {
        DeliveryResourceType resourceType = request.resourceType();
        Long resourceId = request.resourceId();
        DeliveryResourceHandler handler = handlerFactory.get(resourceType);
        handler.startDelivery(resourceId);

        if (deliveryRepository.existsByResourceTypeAndResourceId(resourceType, resourceId)) {
            throw new DuplicateResourceException("Chứng từ này đã có vận đơn.");
        }

        Delivery delivery = Delivery.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .carrierName(clean(request.carrierName()))
                .trackingCode(clean(request.trackingCode()))
                .shippingFee(request.shippingFee() == null ? 0 : request.shippingFee())
                .status(DeliveryStatus.PICKED_UP)
                .build();
        delivery.setCreatedBy(currentUserId);
        delivery.setLastModifiedBy(currentUserId);
        return toResponse(deliveryRepository.saveAndFlush(delivery));
    }

    @Transactional(readOnly = true)
    public PageResponse<DeliveryResponse> getAll(
            DeliveryResourceType resourceType,
            DeliveryStatus status,
            String carrierName,
            String keyword,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    ) {
        validateFilters(fromDate, toDate, pageable);
        Specification<Delivery> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (resourceType != null) predicates.add(cb.equal(root.get("resourceType"), resourceType));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (carrierName != null && !carrierName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("carrierName")), contains(carrierName)));
            }
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), toDate));
            if (keyword != null && !keyword.isBlank()) {
                String pattern = contains(keyword);
                List<Predicate> matches = new ArrayList<>();
                matches.add(cb.like(cb.lower(root.get("trackingCode")), pattern));
                matches.add(cb.like(cb.lower(root.get("carrierName")), pattern));

                Subquery<Long> orderMatch = query.subquery(Long.class);
                Root<com.datn.pc_store.modules.order.entity.Order> order = orderMatch.from(com.datn.pc_store.modules.order.entity.Order.class);
                orderMatch.select(order.get("id")).where(
                        cb.equal(order.get("id"), root.get("resourceId")),
                        cb.equal(root.get("resourceType"), DeliveryResourceType.ORDER),
                        cb.like(cb.lower(order.get("code")), pattern)
                );
                matches.add(cb.exists(orderMatch));

                Subquery<Long> returnMatch = query.subquery(Long.class);
                Root<com.datn.pc_store.modules.purchasereturn.entity.PurchaseReturn> purchaseReturn =
                        returnMatch.from(com.datn.pc_store.modules.purchasereturn.entity.PurchaseReturn.class);
                returnMatch.select(purchaseReturn.get("id")).where(
                        cb.equal(purchaseReturn.get("id"), root.get("resourceId")),
                        cb.equal(root.get("resourceType"), DeliveryResourceType.PURCHASE_RETURN),
                        cb.like(cb.lower(purchaseReturn.get("code")), pattern)
                );
                matches.add(cb.exists(returnMatch));
                predicates.add(cb.or(matches.toArray(Predicate[]::new)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<DeliveryResponse> result = deliveryRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.of(result);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getById(Long id) {
        return toResponse(findDelivery(id));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByResource(DeliveryResourceType resourceType, Long resourceId) {
        Delivery delivery = deliveryRepository.findFirstByResourceTypeAndResourceIdOrderByIdDesc(resourceType, resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Vận đơn", "resourceId", resourceId));
        return toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse updateMetadata(Long id, DeliveryMetadataUpdateRequest request, Long currentUserId) {
        Delivery delivery = deliveryRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vận đơn", "id", id));
        if (isTerminal(delivery.getStatus())) {
            throw new BadRequestException("Không thể sửa thông tin vận đơn đã kết thúc.");
        }
        if (request.getTrackingCode() != null) delivery.setTrackingCode(clean(request.getTrackingCode()));
        if (request.getCarrierName() != null) delivery.setCarrierName(clean(request.getCarrierName()));
        if (request.getShippingFee() != null) delivery.setShippingFee(request.getShippingFee());
        delivery.setLastModifiedBy(currentUserId);
        return toResponse(deliveryRepository.saveAndFlush(delivery));
    }

    @Transactional
    public DeliveryResponse updateStatus(Long id, DeliveryStatus target, Long currentUserId) {
        Delivery delivery = deliveryRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vận đơn", "id", id));
        DeliveryStatus current = delivery.getStatus();
        if (current == target) return toResponse(delivery);
        validateTransition(current, target);

        delivery.setStatus(target);
        delivery.setLastModifiedBy(currentUserId);
        if (isTerminal(target)) {
            handlerFactory.get(delivery.getResourceType()).onTerminalStatus(delivery.getResourceId(), target);
        }
        return toResponse(deliveryRepository.saveAndFlush(delivery));
    }

    @Transactional(readOnly = true)
    public boolean existsForResource(DeliveryResourceType resourceType, Long resourceId) {
        return deliveryRepository.existsByResourceTypeAndResourceId(resourceType, resourceId);
    }

    private Delivery findDelivery(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vận đơn", "id", id));
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .resourceType(delivery.getResourceType())
                .resourceId(delivery.getResourceId())
                .resourceCode(handlerFactory.get(delivery.getResourceType()).getResourceCode(delivery.getResourceId()))
                .trackingCode(delivery.getTrackingCode())
                .carrierName(delivery.getCarrierName())
                .shippingFee(delivery.getShippingFee())
                .status(delivery.getStatus())
                .createdBy(delivery.getCreatedBy())
                .createdDate(delivery.getCreatedDate())
                .lastModifiedBy(delivery.getLastModifiedBy())
                .lastModifiedDate(delivery.getLastModifiedDate())
                .build();
    }

    private void validateFilters(Instant fromDate, Instant toDate, Pageable pageable) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate phải nhỏ hơn hoặc bằng toDate.");
        }
        for (var sort : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(sort.getProperty())) {
                throw new BadRequestException("Trường sắp xếp không hợp lệ: " + sort.getProperty());
            }
        }
    }

    private void validateTransition(DeliveryStatus current, DeliveryStatus target) {
        boolean allowed = switch (current) {
            case PICKED_UP -> target == DeliveryStatus.IN_TRANSIT || target == DeliveryStatus.RETURNED;
            case IN_TRANSIT -> target == DeliveryStatus.DELIVERED || target == DeliveryStatus.RETURNED;
            case DELIVERED, RETURNED -> false;
        };
        if (!allowed) {
            throw new BadRequestException("Không thể chuyển vận đơn từ " + current + " sang " + target + ".");
        }
    }

    private boolean isTerminal(DeliveryStatus status) {
        return status == DeliveryStatus.DELIVERED || status == DeliveryStatus.RETURNED;
    }

    private String contains(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }

    private String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
