package com.ecommerce.controller;

import com.ecommerce.dto.request.UserRoleUpdateRequest;
import com.ecommerce.dto.request.UserStatusUpdateRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PagedResponse;
import com.ecommerce.dto.response.UserAdminDTO;
import com.ecommerce.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AdminUserController — Admin management endpoints for user accounts and RBAC role assignments.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Admin endpoints for user account administration and role assignment")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Get all users with pagination (ADMIN)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserAdminDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<UserAdminDTO> users = adminUserService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get detailed user info by user ID (ADMIN)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminDTO>> getUserById(@PathVariable Long id) {
        UserAdminDTO user = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "Enable or disable a user account (ADMIN)")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserAdminDTO>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        UserAdminDTO user = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User account active status updated"));
    }

    @Operation(summary = "Update RBAC roles assigned to a user (ADMIN)")
    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserAdminDTO>> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        UserAdminDTO user = adminUserService.updateUserRoles(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User roles updated successfully"));
    }
}
