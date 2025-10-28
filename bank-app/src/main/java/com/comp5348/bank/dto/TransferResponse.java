package com.comp5348.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transfer response DTO
 */
public class TransferResponse {
    private String transactionId;
    private String status; // "SUCCESS" or "FAILED"
    private String message;
    private BigDecimal remainingBalance;
    private LocalDateTime timestamp;

    // Constructors
    public TransferResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public TransferResponse(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public TransferResponse(String transactionId, String status, String message, BigDecimal remainingBalance) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.remainingBalance = remainingBalance;
        this.timestamp = LocalDateTime.now();
    }

    // Helper method
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
