package com.ecommerce.service;

import com.ecommerce.dto.request.BrandRequest;
import com.ecommerce.dto.response.BrandDTO;
import com.ecommerce.entity.Brand;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BrandService — handles Brand CRUD operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandDTO> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::mapToBrandDTO)
                .collect(Collectors.toList());
    }

    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToBrandDTO)
                .collect(Collectors.toList());
    }

    public BrandDTO getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "slug", slug));
        return mapToBrandDTO(brand);
    }

    public BrandDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return mapToBrandDTO(brand);
    }

    @Transactional
    public BrandDTO createBrand(BrandRequest request) {
        if (brandRepository.existsByName(request.getName())) {
            throw new BadRequestException("Brand with name '" + request.getName() + "' already exists");
        }

        String slug = CategoryService.toSlug(request.getName());
        if (brandRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand: {}", savedBrand.getName());
        return mapToBrandDTO(savedBrand);
    }

    @Transactional
    public BrandDTO updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        if (!brand.getName().equalsIgnoreCase(request.getName()) && brandRepository.existsByName(request.getName())) {
            throw new BadRequestException("Brand with name '" + request.getName() + "' already exists");
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        if (request.getIsActive() != null) {
            brand.setIsActive(request.getIsActive());
        }

        Brand updatedBrand = brandRepository.save(brand);
        log.info("Updated brand: {}", updatedBrand.getName());
        return mapToBrandDTO(updatedBrand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brandRepository.delete(brand);
        log.info("Deleted brand ID: {}", id);
    }

    // ─── Helper ───────────────────────────────────────────────────

    public BrandDTO mapToBrandDTO(Brand brand) {
        if (brand == null) return null;
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .isActive(brand.getIsActive())
                .createdAt(brand.getCreatedAt())
                .build();
    }
}
