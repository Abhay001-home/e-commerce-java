package com.ecommerce.controller;

import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.request.ChangePasswordRequest;
import com.ecommerce.dto.response.AddressDTO;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.UserDTO;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController — REST endpoints for user profile and address management.
 *
 * Base path: /api/users
 * Security: JWT required on all endpoints (except documented exceptions)
 *
 * Endpoints:
 *  GET    /api/users/me                    — Get current user profile
 *  PATCH  /api/users/me                    — Update profile
 *  PUT    /api/users/me/password           — Change password
 *  GET    /api/users/me/addresses          — List addresses
 *  POST   /api/users/me/addresses          — Add address
 *  PUT    /api/users/me/addresses/{id}     — Update address
 *  DELETE /api/users/me/addresses/{id}     — Delete address
 *  PATCH  /api/users/me/addresses/{id}/default — Set default address
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Profile", description = "User profile and address management APIs")
public class UserController {

    private final UserService userService;

    // ─── Profile ──────────────────────────────────────────────────

    @Operation(summary = "Get current user's profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getProfile(userDetails.getUsername()))
        );
    }

    @Operation(summary = "Update profile (first name, last name, phone)")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(
                        userDetails.getUsername(),
                        updates.get("firstName"),
                        updates.get("lastName"),
                        updates.get("phone")
                ),
                "Profile updated successfully"
        ));
    }

    @Operation(summary = "Change password")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    // ─── Addresses ───────────────────────────────────────────────

    @Operation(summary = "Get all addresses for current user")
    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getAddresses(userDetails.getUsername()))
        );
    }

    @Operation(summary = "Add a new address")
    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        userService.addAddress(userDetails.getUsername(), request),
                        "Address added successfully"
                )
        );
    }

    @Operation(summary = "Update an address")
    @PutMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateAddress(userDetails.getUsername(), addressId, request),
                "Address updated successfully"
        ));
    }

    @Operation(summary = "Delete an address")
    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {
        userService.deleteAddress(userDetails.getUsername(), addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }

    @Operation(summary = "Set an address as default")
    @PatchMapping("/me/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.setDefaultAddress(userDetails.getUsername(), addressId),
                "Default address updated"
        ));
    }
}
