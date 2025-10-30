package com.comp5348.store.service.impl;

import com.comp5348.dto.DeliveryRequest;
import com.comp5348.dto.EmailRequest;
import com.comp5348.dto.PaymentResponse;
import com.comp5348.dto.RefundRequest;
import com.comp5348.store.entity.Order;
import com.comp5348.store.entity.OrderStatus;
import com.comp5348.store.entity.OrderWarehouseAllocation;
import com.comp5348.store.entity.User;
import com.comp5348.store.exception.InsufficientStockException;
import com.comp5348.store.messaging.publisher.OrderEventPublisher;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.service.OrderProcessingService;
import com.comp5348.store.service.StockService;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderProcessingServiceImpl implements OrderProcessingService {
    Logger log = LoggerFactory.getLogger(OrderProcessingServiceImpl.class);
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private StockService stockService;
    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Transactional(rollbackFor = Exception.class) //确保所有 Exception 都触发回滚
    @Override
    public void processPaymentResult(PaymentResponse response) throws Exception { //明抛出异常
        Order order = null;
        // 初始加载订单
        order = orderRepository.findById(response.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + response.getOrderId()));

        // 检查初始状态是否为 PENDING
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            log.warn("Order ID [{}] is not in PENDING status (Current: {}). Ignoring payment result.",
                    order.getId(), order.getOrderStatus());
            return; // 忽略非 PENDING 状态的订单支付结果
        }

        Map<Long, Integer> warehouseAllocation = order.getWarehouseAllocations().stream()
                .collect(Collectors.toMap(
                        allocation -> allocation.getWarehouse().getId(),
                        OrderWarehouseAllocation::getAllocatedQuantity
                ));
        Long productId = order.getOrderItems().get(0).getProduct().getId();
        User user = order.getUser();
        String userEmail = user.getEmail();

        // 处理支付成功
        if ("SUCCESS".equals(response.getPaymentStatus())) {
            log.info("Payment successful for Order ID [{}]. Updating status to PAYMENT_SUCCESS and attempting to allocate stock.", order.getId());

            // 使用条件更新，确保是从 PENDING 更新过来的
            int initialUpdateRows = orderRepository.updateStatusIfCurrentStatusIs(order.getId(), OrderStatus.PAYMENT_SUCCESS, OrderStatus.PENDING);
            if (initialUpdateRows == 0) {
                // 如果更新失败，说明状态已被改变（可能被取消）
                Order currentStatusCheck = orderRepository.findById(order.getId()).orElse(null);
                log.warn("Order ID [{}] status was changed from PENDING concurrently before updating to PAYMENT_SUCCESS. Current status: {}. Ignoring payment result.",
                        order.getId(), (currentStatusCheck != null ? currentStatusCheck.getOrderStatus() : "NOT FOUND"));
                return;
            }
            log.info("Order ID [{}] status updated to PAYMENT_SUCCESS.", order.getId());

            try {
                // 尝试扣减库存 (包含乐观锁检查)
                stockService.decreaseStock(warehouseAllocation, productId);

                try {
                    // 等待
                    log.info("Waiting for 20 seconds to simulate processing sending delivery for Order ID [{}]...", order.getId());
                    TimeUnit.SECONDS.sleep(20);
                    log.info("Wait complete for Order ID [{}]", order.getId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Wait interrupted after stock decrease for Order ID [{}].", order.getId());
                    // 必须抛出异常以触发事务回滚
                    throw new RuntimeException("Wait interrupted, rolling back", e);
                }

                //  原子性地尝试将状态从 PAYMENT_SUCCESS 更新到 AWAITING_SHIPMENT
                int finalUpdateRows = 0;
                try {
                    // 条件更新：期望当前状态是 PAYMENT_SUCCESS
                    finalUpdateRows = orderRepository.updateStatusIfCurrentStatusIs(order.getId(), OrderStatus.AWAITING_SHIPMENT, OrderStatus.PAYMENT_SUCCESS);
                    log.debug("Conditional update from PAYMENT_SUCCESS to AWAITING_SHIPMENT for Order ID [{}] affected {} rows.", order.getId(), finalUpdateRows);
                } catch (Exception e) {
                    log.error("Error during conditional update to AWAITING_SHIPMENT for Order ID [{}]: {}", order.getId(), e.getMessage(), e);
                    // 必须抛出异常以触发事务回滚
                    throw new RuntimeException("DB error during conditional update, rolling back", e);
                }

                //  根据原子更新的结果决定是否发货
                if (finalUpdateRows > 0) {
                    // 更新成功 状态已变为 AWAITING_SHIPMENT
                    log.info("Order ID [{}] status  updated to AWAITING_SHIPMENT. Proceeding with delivery.", order.getId());

                    // 发送发货请求
                    DeliveryRequest deliveryRequest = new DeliveryRequest();
                    deliveryRequest.setOrderId(order.getId());
                    deliveryRequest.setCustomerAddress(user.getAddress());
                    deliveryRequest.setWarehouseAllocations(warehouseAllocation);
                    orderEventPublisher.sendDeliveryRequest(deliveryRequest);
                    log.info("Delivery request sent for Order ID [{}].", order.getId());

                } else {
                    // 更新失败 状态不再是 PAYMENT_SUCCESS 可能是被取消了
                    Order finalStatusCheck = orderRepository.findById(order.getId()).orElse(null);
                    log.warn("Delivery request aborted. Failed to update status to AWAITING_SHIPMENT for Order ID [{}]. Final status: {}.",
                            order.getId(), (finalStatusCheck != null ? finalStatusCheck.getOrderStatus() : "NOT FOUND"));

                    // 必须抛出异常以触发事务回滚 (回滚扣减的库存)
                    throw new RuntimeException("Order status changed concurrently (likely cancelled). Rolling back stock deduction.");
                }

            } catch (InsufficientStockException | OptimisticLockException |
                     ObjectOptimisticLockingFailureException e) {
                // 库存扣减失败
                log.error("Failed to decrease stock for Order ID [{}]: {}. Cancelling order and initiating refund.", order.getId(), e.getMessage());

                // 发送库存失败邮件
                EmailRequest email = new EmailRequest();
                email.setTo(userEmail);
                email.setSubject("Order Processing Failed: " + order.getId());
                if (e instanceof InsufficientStockException) {
                    email.setBody("We're sorry, but the item for your order ID " + order.getId() + " is out of stock after your payment was processed. The order has been cancelled and a full refund will be processed.");
                } else { // 乐观锁异常
                    email.setBody("We encountered a temporary issue processing the stock for your order ID " + order.getId() + " after your payment was processed. The order has been cancelled and a refund will be processed. Please try placing the order again later.");
                }
                orderEventPublisher.sendEmailRequest(email);

                // 发起退款请求
                RefundRequest refundRequest = new RefundRequest();
                refundRequest.setOrderId(order.getId());
                refundRequest.setAmount(order.getTotalPrice());
                refundRequest.setCustomerBankAccountNumber(user.getBankAccountNumber());
                orderEventPublisher.sendRefundRequest(refundRequest);

                log.info("Refund request sent for failed order ID [{}] due to stock issue.", order.getId());

                // 必须抛出异常以触发事务回滚
                // (回滚 "PAYMENT_SUCCESS" 的状态)
                throw new RuntimeException("Stock operation failed, rolling back status.", e);
            }

        } else { // 支付失败
            log.warn("Payment failed for Order ID [{}]. Updating status to PAYMENT_FAILED.", order.getId());
            // 使用条件更新，期望当前状态是 PENDING
            int failedRows = orderRepository.updateStatusIfCurrentStatusIs(order.getId(), OrderStatus.PAYMENT_FAILED, OrderStatus.PENDING);
            log.info("Conditional update to PAYMENT_FAILED for Order ID [{}] affected {} rows.", order.getId(), failedRows);

            if (failedRows > 0) {
                EmailRequest email = new EmailRequest();
                email.setTo(userEmail);
                email.setSubject("Order Payment Failed: " + order.getId());
                email.setBody("Your payment for order ID " + order.getId() + " has failed. The order has been cancelled. Please check your payment details or contact support.");
                orderEventPublisher.sendEmailRequest(email);
            } else {
                Order currentStatusCheck = orderRepository.findById(order.getId()).orElse(null);
                log.warn("Conditional update to PAYMENT_FAILED failed for Order ID [{}]. Status was likely changed from PENDING concurrently. Current status: {}",
                        order.getId(), (currentStatusCheck != null ? currentStatusCheck.getOrderStatus() : "NOT FOUND"));
            }
            // 支付失败也是一种“成功处理”的业务流程，所以这里不需要抛异常
        }
    }
}