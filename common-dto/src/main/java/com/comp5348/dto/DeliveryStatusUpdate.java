package com.comp5348.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class DeliveryStatusUpdate implements Serializable {
    private Long orderId;
    private String deliveryStatus;
    private LocalDateTime updateTime;
}
