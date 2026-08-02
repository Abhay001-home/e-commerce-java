package com.ecommerce.dto.request;

import com.ecommerce.entity.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ShipmentUpdateRequest DTO — Payload for setting tracking/carrier info on a shipment.
 */
@Data
public class ShipmentUpdateRequest {

    @NotBlank(message = "Carrier name is required")
    private String carrierName;

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    @NotNull(message = "Shipment status is required")
    private ShipmentStatus shipmentStatus;

    private LocalDateTime estimatedDeliveryDate;
}
