package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * BrandRequest DTO — create or update a brand.
 */
@Data
public class BrandRequest {

    @NotBlank(message = "Brand name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private String description;

    private String logoUrl;

    private Boolean isActive = true;
}
