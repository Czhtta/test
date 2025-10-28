package com.comp5348.bank.controller;

import com.comp5348.bank.dto.TransferRequest;
import com.comp5348.bank.dto.TransferResponse;
import com.comp5348.bank.service.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Bank operations
 */
@RestController
@RequestMapping("/api/bank")
@CrossOrigin(origins = "*")
public class BankController {

    private static final Logger logger = LoggerFactory.getLogger(BankController.class);

    @Autowired
    private BankService bankService;

    /**
     * Process payment from customer to store
     */
    @PostMapping("/payment")
    public ResponseEntity<TransferResponse> processPayment(@RequestBody TransferRequest request) {
        logger.info("Received payment request for order {}: {} -> {} amount: {}",
                request.getOrderId(), request.getFromAccount(),
                request.getToAccount(), request.getAmount());

        try {
            TransferResponse response = bankService.processPayment(request);

            if (response.isSuccessful()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Payment processing failed: ", e);
            TransferResponse errorResponse = new TransferResponse(
                    null,
                    "FAILED",
                    "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Process refund from store to customer
     */
    @PostMapping("/refund")
    public ResponseEntity<TransferResponse> processRefund(@RequestBody TransferRequest request) {
        logger.info("Received refund request for order {}: {} -> {} amount: {}",
                request.getOrderId(), request.getFromAccount(),
                request.getToAccount(), request.getAmount());

        try {
            TransferResponse response = bankService.processRefund(request);

            if (response.isSuccessful()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Refund processing failed: ", e);
            TransferResponse errorResponse = new TransferResponse(
                    null,
                    "FAILED",
                    "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get account balance
     */
    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String accountNumber) {
        logger.info("Checking balance for account: {}", accountNumber);

        try {
            BigDecimal balance = bankService.getBalance(accountNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("accountNumber", accountNumber);
            response.put("balance", balance);
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get balance: ", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Bank Service");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}