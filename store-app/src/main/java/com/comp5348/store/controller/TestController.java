package com.comp5348.store.controller;

import com.comp5348.store.entity.User;
import com.comp5348.store.entity.Warehouse;
import com.comp5348.store.entity.Stock;
import com.comp5348.store.entity.Product;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.repository.WarehouseRepository;
import com.comp5348.store.repository.StockRepository;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockService stockService;

    @PostMapping("/create-user")
    public User createTestUser(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }

    @GetMapping("/create-user")
    public User createTestUserGet(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }

    @GetMapping("/users")
    public Object getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/create-warehouse")
    public Warehouse createWarehouse(@RequestParam String name, @RequestParam String location) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(name);
        warehouse.setLocation(location);
        return warehouseRepository.save(warehouse);
    }

    @PostMapping("/create-stock")
    public String createStock(@RequestParam Long warehouseId, @RequestParam Long productId, @RequestParam Integer quantity) {
        try {
            stockService.updateStockQuantity(warehouseId, productId, quantity);
            return "Stock created successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/warehouses")
    public Object getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @GetMapping("/stocks")
    public Object getAllStocks() {
        return stockRepository.findAll();
    }
}
