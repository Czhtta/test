package com.comp5348.store.controller;

import com.comp5348.dto.DeliveryRequest;
import com.comp5348.dto.EmailRequest;
import com.comp5348.store.entity.User;
import com.comp5348.store.entity.Warehouse;
import com.comp5348.store.entity.Stock;
import com.comp5348.store.entity.Product;
import com.comp5348.store.messaging.publisher.OrderEventPublisher;
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
    private OrderEventPublisher orderEventPublisher;

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

    @GetMapping("/send-test-email")
    public Map<String, String> sendTestEmail(){
        EmailRequest testEmail = new EmailRequest();
        testEmail.setTo("xxxxx");
        testEmail.setSubject("Hello World!");
        testEmail.setBody("This is a test message!");

        orderEventPublisher.sendEmailRequest(testEmail);

        return Map.of("status", "Test email message sent!");
    }
    @GetMapping("/send-test-delivery")
    public Map<String,String> sendTestDelivery(){
        DeliveryRequest testDelivery = new DeliveryRequest();
        testDelivery.setOrderId(999L);
        testDelivery.setCustomerAddress("123 Fake Street, Sydney, Australia");
        testDelivery.setWarehouseAllocations(Map.of(101L, 5, 102L, 2));
        orderEventPublisher.sendDeliveryRequest(testDelivery);
        return Map.of("status", "Test delivery message sent!");
    }
}
