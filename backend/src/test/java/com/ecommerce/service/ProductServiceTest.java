package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductDetailDTO;
import com.ecommerce.entity.Brand;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.BrandRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private InventoryService inventoryService;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    private ProductRequest productRequest;
    private Product product;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest();
        productRequest.setName("Galaxy S24");
        productRequest.setPrice(new BigDecimal("79999.00"));
        productRequest.setMrp(new BigDecimal("89999.00"));
        productRequest.setCategoryId(1L);
        productRequest.setBrandId(1L);
        productRequest.setQuantity(50);

        Category category = Category.builder().id(1L).name("Smartphones").slug("smartphones").build();
        Brand brand = Brand.builder().id(1L).name("Samsung").slug("samsung").build();

        product = Product.builder()
                .id(100L)
                .name("Galaxy S24")
                .slug("galaxy-s24")
                .price(new BigDecimal("79999.00"))
                .mrp(new BigDecimal("89999.00"))
                .category(category)
                .brand(brand)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should create product with inventory successfully")
    void createProduct_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(product.getCategory()));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(product.getBrand()));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDetailDTO result = productService.createProduct(productRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Galaxy S24");
        assertThat(result.getCategoryName()).isEqualTo("Smartphones");
        assertThat(result.getBrandName()).isEqualTo("Samsung");

        verify(productRepository).save(any(Product.class));
        verify(inventoryService).createDefaultInventory(any(Product.class), eq(50), any());
    }

    @Test
    @DisplayName("Should fetch product by slug")
    void getProductBySlug_Success() {
        when(productRepository.findBySlug("galaxy-s24")).thenReturn(Optional.of(product));

        ProductDetailDTO result = productService.getProductBySlug("galaxy-s24");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getSlug()).isEqualTo("galaxy-s24");
    }
}
