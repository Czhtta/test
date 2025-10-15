package com.comp5348.store.controller;


import com.comp5348.store.dto.UpdateStockRequest;
import com.comp5348.store.service.StockService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/admin/stocks")
public class StockAdminController {
    private final StockService stockService;

    @Autowired
    public StockAdminController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ResponseEntity<?> updateStock(@RequestBody UpdateStockRequest updateStockRequest) {
        try {
            stockService.updateStockQuantity(updateStockRequest.getProductId(),
                                                updateStockRequest.getWarehouseId(),
                                                updateStockRequest.getQuantity());
            return ResponseEntity.ok("Stock updated successfully.");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating stock: " + e.getMessage());
        }
    }
}
