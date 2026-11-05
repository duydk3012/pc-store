package com.datn.pc_store.modules.warranty.mapper;

import com.datn.pc_store.modules.warranty.dto.WarrantyPolicyCreateRequest;
import com.datn.pc_store.modules.warranty.dto.WarrantyPolicyResponse;
import com.datn.pc_store.modules.warranty.dto.WarrantyPolicyUpdateRequest;
import com.datn.pc_store.modules.warranty.entity.WarrantPolicy;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarrantyPolicyMapper {

    @Mapping(target = "id", ignore = true)
    WarrantPolicy toEntity(WarrantyPolicyCreateRequest req);

    WarrantyPolicyResponse toResponse(WarrantPolicy entity);

    List<WarrantyPolicyResponse> toResponseList(List<WarrantPolicy> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(WarrantyPolicyUpdateRequest req, @MappingTarget WarrantPolicy entity);
}
