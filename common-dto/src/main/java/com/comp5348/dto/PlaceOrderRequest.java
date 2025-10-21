package com.comp5348.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class PlaceOrderRequest implements Serializable {
    private Long productId;
    private int quantity;
}
