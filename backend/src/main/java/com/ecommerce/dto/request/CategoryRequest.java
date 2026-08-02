package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * CategoryRequest DTO — create or update a category.
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private String description;

    private String imageUrl;

    /** Parent category ID — null for root categories. */
    private Long parentId;

    private Boolean isActive = true;
}
