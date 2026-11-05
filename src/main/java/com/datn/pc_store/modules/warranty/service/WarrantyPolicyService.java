package com.datn.pc_store.modules.warranty.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.category.repository.CategoryRepository;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import com.datn.pc_store.modules.warranty.dto.*;
import com.datn.pc_store.modules.warranty.entity.WarrantPolicy;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyStatus;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyTargetType;
import com.datn.pc_store.modules.warranty.mapper.WarrantyPolicyMapper;
import com.datn.pc_store.modules.warranty.repository.WarrantyPolicyRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarrantyPolicyService {
    private final WarrantyPolicyRepository repository;
    private final WarrantyPolicyMapper mapper;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public PageResponse<WarrantyPolicyResponse> getAllPolicies(String keyword, WarrantyPolicyStatus status,
            WarrantyPolicyTargetType targetType, Long targetId, Pageable pageable) {
        return PageResponse.of(repository.findAll((root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT)
                        .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), pattern, '!'),
                        cb.like(cb.lower(root.get("description")), pattern, '!')));
            }
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (targetType != null) predicates.add(cb.equal(root.get("targetType"), targetType));
            if (targetId != null) predicates.add(cb.equal(root.get("targetId"), targetId));
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable).map(mapper::toResponse));
    }

    public WarrantyPolicyResponse getPolicyById(Long id) {
        return mapper.toResponse(findPolicy(id));
    }

    @Transactional
    public WarrantyPolicyResponse createPolicy(WarrantyPolicyCreateRequest req) {
        validateTarget(req.getTargetType(), req.getTargetId());
        WarrantPolicy policy = mapper.toEntity(req);
        policy.setName(req.getName().trim());
        if (req.getStatus() == null) policy.setStatus(WarrantyPolicyStatus.ACTIVE);
        return mapper.toResponse(repository.save(policy));
    }

    @Transactional
    public WarrantyPolicyResponse updatePolicy(Long id, WarrantyPolicyUpdateRequest req) {
        WarrantPolicy policy = findPolicy(id);
        validateTarget(req.getTargetType(), req.getTargetId());
        mapper.updateEntityFromDto(req, policy);
        policy.setName(req.getName().trim());
        // PUT can clear the scope and description; omitted status keeps its previous value.
        policy.setTargetType(req.getTargetType());
        policy.setTargetId(req.getTargetId());
        policy.setDescription(req.getDescription());
        return mapper.toResponse(repository.save(policy));
    }

    @Transactional
    public WarrantyPolicyResponse updateStatus(Long id, WarrantyPolicyStatus status) {
        WarrantPolicy policy = findPolicy(id);
        policy.setStatus(status);
        return mapper.toResponse(repository.save(policy));
    }

    @Transactional
    public void deletePolicy(Long id) {
        repository.delete(findPolicy(id));
    }

    private WarrantPolicy findPolicy(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chính sách bảo hành", "id", id));
    }

    private void validateTarget(WarrantyPolicyTargetType type, Long id) {
        if (type == null && id == null) return;
        if (type == null || id == null) {
            throw new BadRequestException("targetType và targetId phải được cung cấp cùng nhau");
        }
        if (type == WarrantyPolicyTargetType.CATEGORY) {
            if (!categoryRepository.existsById(id)) throw new ResourceNotFoundException("Danh mục", "id", id);
        } else if (type == WarrantyPolicyTargetType.PRODUCT) {
            if (!productRepository.existsById(id)) throw new ResourceNotFoundException("Sản phẩm", "id", id);
        } else {
            throw new BadRequestException("targetType chỉ nhận 0 (danh mục) hoặc 1 (sản phẩm)");
        }
    }
}
