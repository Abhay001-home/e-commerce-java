package com.ecommerce.service;

import com.ecommerce.dto.response.InventoryDTO;
import com.ecommerce.entity.Inventory;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * InventoryService — manages stock levels, low stock alerts, and updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", "productId", productId));
        return mapToInventoryDTO(inventory);
    }

    @Transactional
    public InventoryDTO updateStock(Long productId, Integer quantity, Integer lowStockQty) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", "productId", productId));

        if (quantity != null) {
            inventory.setQuantity(quantity);
        }
        if (lowStockQty != null) {
            inventory.setLowStockQty(lowStockQty);
        }

        Inventory updated = inventoryRepository.save(inventory);
        log.info("Updated stock for product ID {}: new qty={}", productId, updated.getQuantity());
        return mapToInventoryDTO(updated);
    }

    public List<InventoryDTO> getLowStockAlerts() {
        return inventoryRepository.findLowStockInventory().stream()
                .map(this::mapToInventoryDTO)
                .collect(Collectors.toList());
    }

    public List<InventoryDTO> getOutOfStockAlerts() {
        return inventoryRepository.findOutOfStockInventory().stream()
                .map(this::mapToInventoryDTO)
                .collect(Collectors.toList());
    }

    public Inventory createDefaultInventory(Product product, Integer quantity, Integer lowStockQty) {
        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(quantity != null ? quantity : 0)
                .lowStockQty(lowStockQty != null ? lowStockQty : 10)
                .build();
        return inventoryRepository.save(inventory);
    }

    public InventoryDTO mapToInventoryDTO(Inventory inventory) {
        if (inventory == null) return null;
        return InventoryDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())
                .quantity(inventory.getQuantity())
                .lowStockQty(inventory.getLowStockQty())
                .inStock(inventory.isInStock())
                .lowStock(inventory.isLowStock())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
