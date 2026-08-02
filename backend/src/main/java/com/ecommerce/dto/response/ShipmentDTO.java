package com.ecommerce.dto.response;

import com.ecommerce.entity.ShipmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ShipmentDTO — response representation of physical shipment details.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipmentDTO {

    private Long id;
    private String carrierName;
    private String trackingNumber;
    private ShipmentStatus shipmentStatus;
    private LocalDateTime shippedAt;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveredAt;
}
