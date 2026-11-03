package com.datn.pc_store.modules.user.service;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.DuplicateResourceException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.auth.service.RefreshTokenService;
import com.datn.pc_store.modules.user.dto.*;
import com.datn.pc_store.modules.user.entity.User;
import com.datn.pc_store.modules.user.enums.RoleType;
import com.datn.pc_store.modules.user.enums.UserStatus;
import com.datn.pc_store.modules.user.mapper.UserMapper;
import com.datn.pc_store.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "username", "fullName", "email", "phoneNumber", "address",
            "citizenIdentificationCard", "role", "status", "createdDate", "createdBy",
            "lastModifiedDate", "lastModifiedBy"
    );

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(String keyword, RoleType role, UserStatus status, Pageable pageable) {
        for (var order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("Trường sắp xếp không hợp lệ: " + order.getProperty()
                        + ". Sử dụng sort=createdDate,desc hoặc bỏ tham số sort để dùng mặc định.");
            }
        }
        return PageResponse.of(userRepository.searchUsers(keyword, role, status, pageable).map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException("Người dùng", "username", req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Người dùng", "email", req.getEmail());
        }

        User user = userMapper.toEntity(req);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole() != null ? req.getRole() : RoleType.CUSTOMER);
        user.setStatus(req.getStatus() != null ? req.getStatus() : UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        log.info("Đã tạo người dùng id: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest req) {
        User user = findUser(id);
        if (userRepository.existsByUsernameAndIdNot(req.getUsername(), id)) {
            throw new DuplicateResourceException("Người dùng", "username", req.getUsername());
        }
        if (userRepository.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new DuplicateResourceException("Người dùng", "email", req.getEmail());
        }

        boolean revokeSessions = !user.getUsername().equals(req.getUsername())
                || req.getPassword() != null
                || (req.getRole() != null && req.getRole() != user.getRole())
                || (req.getStatus() != null && req.getStatus() != user.getStatus());

        if (!user.getEmail().equalsIgnoreCase(req.getEmail())) {
            user.setEmailVerified(false);
        }
        userMapper.updateEntityFromDto(req, user);
        if (req.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getRole() != null) {
            user.setRole(req.getRole());
        }
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        if (revokeSessions) {
            revokeSessions(user);
        }

        User updated = userRepository.save(user);
        log.info("Đã cập nhật người dùng id: {}", id);
        return userMapper.toResponse(updated);
    }

    @Transactional
    public UserResponse updateStatus(Long id, UserStatus status) {
        User user = findUser(id);
        if (user.getStatus() != status) {
            user.setStatus(status);
            revokeSessions(user);
        }
        log.info("Cập nhật trạng thái người dùng id: {} sang: {}", id, status);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUser(id);
        revokeSessions(user);
        userRepository.saveAndFlush(user);
        userRepository.delete(user);
        log.info("Đã xóa mềm người dùng id: {}", id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", id));
    }

    private void revokeSessions(User user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
        refreshTokenService.revokeAllByUser(user.getId());
    }
}
