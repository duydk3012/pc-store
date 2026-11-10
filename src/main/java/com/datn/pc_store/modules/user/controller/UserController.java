package com.datn.pc_store.modules.user.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.user.dto.*;
import com.datn.pc_store.modules.user.enums.RoleType;
import com.datn.pc_store.modules.user.enums.UserStatus;
import com.datn.pc_store.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Users (Admin)", description = "Quản lý tài khoản người dùng dành cho Quản trị viên (Admin)")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm người dùng",
            description = "Lấy danh sách người dùng chưa bị xóa có phân trang; tìm theo tên đăng nhập, họ tên, email hoặc số điện thoại và lọc theo vai trò, trạng thái. "
                    + "Sắp xếp bằng sort=createdDate,desc; có thể lặp lại tham số sort để sắp xếp nhiều trường. "
                    + "Các trường cho phép: id, username, fullName, email, phoneNumber, address, citizenIdentificationCard, role, status, createdDate, createdBy, lastModifiedDate, lastModifiedBy. "
                    + "Không gửi sort nếu muốn dùng mặc định createdDate,desc; không gửi mảng JSON như [] hoặc [\"string\"].")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Từ khóa tìm kiếm (tên đăng nhập, họ tên, email, số điện thoại)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo vai trò tài khoản")
            @RequestParam(required = false) RoleType role,
            @Parameter(description = "Lọc theo trạng thái: ACTIVE, INACTIVE hoặc BANNED")
            @RequestParam(required = false) UserStatus status,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getAllUsers(keyword, role, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết người dùng theo ID",
            description = "Lấy thông tin tài khoản chưa bị xóa theo ID, không trả về mật khẩu, token hoặc phiên bản token.")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID người dùng") @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "Thêm mới người dùng",
            description = "Tạo tài khoản với tên đăng nhập và email duy nhất. Mật khẩu được mã hóa; vai trò mặc định CUSTOMER và trạng thái mặc định ACTIVE. Trả về 201 khi thành công, 400 nếu dữ liệu không hợp lệ, 409 nếu tên đăng nhập hoặc email trùng.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa thông tin người dùng",
            description = "Cập nhật tài khoản theo ID. Mật khẩu, vai trò và trạng thái giữ nguyên nếu không gửi; các trường liên hệ tùy chọn có thể gửi null để xóa. Thay đổi tên đăng nhập, mật khẩu, vai trò hoặc trạng thái sẽ thu hồi toàn bộ phiên đăng nhập. Trả về 404 nếu không tìm thấy, 409 nếu tên đăng nhập hoặc email trùng.")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID người dùng cần sửa") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest req
    ) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái người dùng",
            description = "Cập nhật ACTIVE (hoạt động), INACTIVE (ngưng hoạt động) hoặc BANNED (bị cấm). Khi trạng thái thay đổi, hệ thống vô hiệu hóa access token và thu hồi toàn bộ refresh token của tài khoản.")
    public ResponseEntity<UserResponse> updateStatus(
            @Parameter(description = "ID người dùng") @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(userService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng (Xóa mềm)",
            description = "Đánh dấu xóa mềm tài khoản, vô hiệu hóa access token và thu hồi toàn bộ refresh token. Tên đăng nhập và email vẫn được giữ trong cơ sở dữ liệu và không thể đăng ký lại. Trả về 204 khi thành công hoặc 404 nếu không tìm thấy.")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID người dùng cần xóa") @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
