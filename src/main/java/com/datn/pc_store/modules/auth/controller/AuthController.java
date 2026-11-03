package com.datn.pc_store.modules.auth.controller;

import com.datn.pc_store.modules.auth.dto.AuthResponse;
import com.datn.pc_store.modules.auth.dto.CurrentUserResponse;
import com.datn.pc_store.modules.auth.dto.LoginRequest;
import com.datn.pc_store.modules.auth.dto.RefreshTokenRequest;
import com.datn.pc_store.modules.auth.dto.RegisterRequest;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập, quản lý token và thông tin tài khoản hiện tại")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản với vai trò CUSTOMER và trạng thái ACTIVE, trả về access token và refresh token để đăng nhập ngay. Không yêu cầu xác thực.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đăng ký không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "409", description = "Tên đăng nhập hoặc email đã tồn tại", content = @Content)
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Đăng nhập", description = "Xác thực bằng tên đăng nhập và mật khẩu, trả về access token, refresh token và thông tin tài khoản. Không yêu cầu xác thực trước đó.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đăng nhập không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Sai tên đăng nhập hoặc mật khẩu", content = @Content),
            @ApiResponse(responseCode = "403", description = "Tài khoản ngưng hoạt động hoặc đã bị cấm", content = @Content)
    })
    public AuthResponse login(
            @Valid @RequestBody LoginRequest req
    ) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Làm mới token", description = "Dùng refresh token còn hiệu lực để cấp cặp token mới và thu hồi refresh token cũ. Nếu dùng lại token đã thu hồi, hệ thống thu hồi toàn bộ refresh token của tài khoản. Không yêu cầu access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cấp cặp token mới thành công"),
            @ApiResponse(responseCode = "400", description = "Refresh token không được để trống", content = @Content),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ, đã hết hạn, đã bị thu hồi hoặc người dùng không tồn tại", content = @Content),
            @ApiResponse(responseCode = "403", description = "Tài khoản không còn hoạt động", content = @Content)
    })
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    @SecurityRequirements
    @Operation(summary = "Đăng xuất thiết bị hiện tại", description = "Thu hồi refresh token được gửi lên. Vẫn trả về thành công nếu token không tồn tại hoặc đã bị thu hồi. Access token hiện tại vẫn có hiệu lực đến khi hết hạn. Không yêu cầu access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Đăng xuất thành công, không có nội dung trả về", content = @Content),
            @ApiResponse(responseCode = "400", description = "Refresh token không được để trống", content = @Content)
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Đăng xuất tất cả thiết bị", description = "Thu hồi toàn bộ refresh token và tăng phiên bản token để vô hiệu hóa các access token đã cấp của tài khoản hiện tại. Yêu cầu Bearer access token hợp lệ.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Đăng xuất tất cả thiết bị thành công, không có nội dung trả về", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực hoặc access token không hợp lệ", content = @Content)
    })
    public ResponseEntity<Void> logoutAll(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    ) {
        authService.logoutAll(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Xem thông tin tài khoản hiện tại", description = "Trả về ID, tên đăng nhập và danh sách quyền của tài khoản đang đăng nhập. Yêu cầu Bearer access token hợp lệ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin tài khoản thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrentUserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực hoặc access token không hợp lệ", content = @Content)
    })
    public CurrentUserResponse me(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return new CurrentUserResponse(
                principal.getId(),
                principal.getUsername(),
                principal.getAuthorities().stream()
                        .map(Object::toString).toList(),
                principal.isEmailVerified()
        );
    }
}
