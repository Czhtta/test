package com.comp5348.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class RefundRequest implements Serializable {
    private Long orderId;
    private BigDecimal amount;
    private String originalTransactionId; //暂时没用到
    private String customerBankAccountNumber;
}
