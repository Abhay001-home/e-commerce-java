package com.ecommerce.controller;

import com.ecommerce.dto.request.BrandRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.BrandDTO;
import com.ecommerce.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BrandController — REST endpoints for Brand management.
 */
@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "Public and Admin Brand APIs")
public class BrandController {

    private final BrandService brandService;

    @Operation(summary = "Get all active brands (ordered by name)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandDTO>>> getActiveBrands() {
        return ResponseEntity.ok(ApiResponse.success(brandService.getAllActiveBrands()));
    }

    @Operation(summary = "Get brand by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<BrandDTO>> getBrandBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrandBySlug(slug)));
    }

    @Operation(summary = "Get brand by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandDTO>> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrandById(id)));
    }

    @Operation(summary = "Create a new brand (ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<BrandDTO>> createBrand(
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(brandService.createBrand(request), "Brand created successfully"));
    }

    @Operation(summary = "Update a brand (ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<BrandDTO>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brandService.updateBrand(id, request), "Brand updated successfully"));
    }

    @Operation(summary = "Delete a brand (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Brand deleted successfully"));
    }
}
