package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.PagedResponse;
import com.ecommerce.dto.response.ProductDTO;
import com.ecommerce.dto.response.ProductDetailDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.BrandRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductService — core business logic for products, variants, images, specs, and dynamic search/filter.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final InventoryService inventoryService;
    private final FileStorageService fileStorageService;

    // ─── Public Product Queries ───────────────────────────────────

    public PagedResponse<ProductDTO> searchAndFilterProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Boolean isFeatured,
            Boolean isActive,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(getSortProperty(sortBy)).ascending()
                : Sort.by(getSortProperty(sortBy)).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        ProductSpecification spec = ProductSpecification.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStock(inStock)
                .isFeatured(isFeatured)
                .isActive(isActive)
                .build();

        Page<Product> productPage = productRepository.findAll(spec.toSpec(), pageable);
        Page<ProductDTO> dtoPage = productPage.map(this::mapToProductDTO);

        return PagedResponse.from(dtoPage);
    }

    public ProductDetailDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return mapToProductDetailDTO(product);
    }

    public ProductDetailDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToProductDetailDTO(product);
    }

    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrueOrderBySoldCountDesc().stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());
    }

    // ─── Admin CRUD Operations ────────────────────────────────────

    @Transactional
    public ProductDetailDTO createProduct(ProductRequest request) {
        if (request.getSku() != null && !request.getSku().isBlank() && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
        }

        String slug = CategoryService.toSlug(request.getName());
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .shortDesc(request.getShortDesc())
                .sku(request.getSku())
                .price(request.getPrice())
                .mrp(request.getMrp())
                .category(category)
                .brand(brand)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .build();

        product.recalculateDiscount();

        // Add Variants if provided
        if (request.getVariants() != null) {
            for (ProductRequest.VariantRequest vr : request.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .variantName(vr.getVariantName())
                        .sku(vr.getSku())
                        .price(vr.getPrice())
                        .mrp(vr.getMrp())
                        .imageUrl(vr.getImageUrl())
                        .isActive(vr.getIsActive() != null ? vr.getIsActive() : true)
                        .build();
                product.addVariant(variant);
            }
        }

        // Add Specifications if provided
        if (request.getSpecifications() != null) {
            for (ProductRequest.SpecificationRequest sr : request.getSpecifications()) {
                com.ecommerce.entity.ProductSpecification spec = com.ecommerce.entity.ProductSpecification.builder()
                        .specKey(sr.getSpecKey())
                        .specValue(sr.getSpecValue())
                        .sortOrder(sr.getSortOrder() != null ? sr.getSortOrder() : 0)
                        .build();
                product.addSpecification(spec);
            }
        }

        Product savedProduct = productRepository.save(product);

        // Create Inventory
        inventoryService.createDefaultInventory(savedProduct, request.getQuantity(), request.getLowStockQty());

        log.info("Created product: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
        return mapToProductDetailDTO(savedProduct);
    }

    @Transactional
    public ProductDetailDTO updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setShortDesc(request.getShortDesc());
        product.setPrice(request.getPrice());
        product.setMrp(request.getMrp());
        product.setCategory(category);
        product.setBrand(brand);
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());

        product.recalculateDiscount();

        Product updatedProduct = productRepository.save(product);
        log.info("Updated product ID: {}", id);
        return mapToProductDetailDTO(updatedProduct);
    }

    @Transactional
    public ProductDetailDTO uploadProductImage(Long productId, MultipartFile file, Boolean isPrimary, Integer sortOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        String imageUrl = fileStorageService.storeFile(file);

        if (Boolean.TRUE.equals(isPrimary)) {
            // Unset current primary image
            product.getImages().forEach(img -> img.setIsPrimary(false));
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .altText(product.getName())
                .isPrimary(isPrimary != null ? isPrimary : product.getImages().isEmpty())
                .sortOrder(sortOrder != null ? sortOrder : product.getImages().size())
                .build();

        product.addImage(image);
        Product savedProduct = productRepository.save(product);
        return mapToProductDetailDTO(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
        log.info("Deleted product ID: {}", id);
    }

    // ─── Mapping Helpers ──────────────────────────────────────────

    public ProductDTO mapToProductDTO(Product product) {
        if (product == null) return null;

        boolean inStock = product.getInventory() != null && product.getInventory().isInStock();
        Integer qty = product.getInventory() != null ? product.getInventory().getQuantity() : 0;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .shortDesc(product.getShortDesc())
                .sku(product.getSku())
                .price(product.getPrice())
                .mrp(product.getMrp())
                .discountPct(product.getDiscountPct())
                .primaryImageUrl(product.getPrimaryImageUrl())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .avgRating(product.getAvgRating())
                .reviewCount(product.getReviewCount())
                .soldCount(product.getSoldCount())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .inStock(inStock)
                .stockQuantity(qty)
                .createdAt(product.getCreatedAt())
                .build();
    }

    public ProductDetailDTO mapToProductDetailDTO(Product product) {
        if (product == null) return null;

        List<ProductDetailDTO.ImageDTO> imageDTOs = product.getImages().stream()
                .map(img -> ProductDetailDTO.ImageDTO.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .altText(img.getAltText())
                        .isPrimary(img.getIsPrimary())
                        .sortOrder(img.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        List<ProductDetailDTO.VariantDTO> variantDTOs = product.getVariants().stream()
                .map(v -> ProductDetailDTO.VariantDTO.builder()
                        .id(v.getId())
                        .variantName(v.getVariantName())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .mrp(v.getMrp())
                        .imageUrl(v.getImageUrl())
                        .isActive(v.getIsActive())
                        .build())
                .collect(Collectors.toList());

        List<ProductDetailDTO.SpecDTO> specDTOs = product.getSpecifications().stream()
                .map(s -> ProductDetailDTO.SpecDTO.builder()
                        .id(s.getId())
                        .specKey(s.getSpecKey())
                        .specValue(s.getSpecValue())
                        .sortOrder(s.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        boolean inStock = product.getInventory() != null && product.getInventory().isInStock();
        Integer qty = product.getInventory() != null ? product.getInventory().getQuantity() : 0;

        return ProductDetailDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDesc(product.getShortDesc())
                .sku(product.getSku())
                .price(product.getPrice())
                .mrp(product.getMrp())
                .discountPct(product.getDiscountPct())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .brandSlug(product.getBrand() != null ? product.getBrand().getSlug() : null)
                .images(imageDTOs)
                .variants(variantDTOs)
                .specifications(specDTOs)
                .inStock(inStock)
                .stockQuantity(qty)
                .avgRating(product.getAvgRating())
                .reviewCount(product.getReviewCount())
                .soldCount(product.getSoldCount())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private String getSortProperty(String sortBy) {
        if ("price".equalsIgnoreCase(sortBy)) return "price";
        if ("rating".equalsIgnoreCase(sortBy)) return "avgRating";
        if ("popularity".equalsIgnoreCase(sortBy) || "bestselling".equalsIgnoreCase(sortBy)) return "soldCount";
        if ("newest".equalsIgnoreCase(sortBy)) return "createdAt";
        return "createdAt";
    }
}
