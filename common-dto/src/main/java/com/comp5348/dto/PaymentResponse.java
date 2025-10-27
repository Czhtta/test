package com.comp5348.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentResponse implements Serializable {
    private  Long orderId;
    private  String paymentStatus;
    private  String transactionId;
}
