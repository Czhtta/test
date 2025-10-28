package com.comp5348.deliverycoapp.service;

import com.comp5348.deliverycoapp.publisher.DeliveryStatusPublisher;
import com.comp5348.dto.DeliveryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SimulationService {
    public static final Logger log = LoggerFactory.getLogger(SimulationService.class);
    private final Random random = new Random();
    private static final String STATUS_PICKED_UP = "PICKED_UP";
    private static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_LOST = "LOST";

    private static final Set<Long> cancelledOrders = ConcurrentHashMap.newKeySet();
    @Autowired
    private DeliveryStatusPublisher statusPublisher;
    /**
     * 标记一个订单已被取消。
     * 这个方法会被 DeliveryCancellationListener 调用。
     * @param orderId 被取消的订单ID
     */
    public void markOrderAsCancelled(Long orderId) {
        if (orderId != null) {
            cancelledOrders.add(orderId);
            log.info("Order [{}] marked as cancelled for delivery simulation.", orderId);
        }
    }
    /**
     * 检查一个订单是否已被标记为取消。
     * @param orderId 要检查的订单ID
     * @return 如果已取消则返回 true，否则 false
     */
    private boolean isOrderCancelled(Long orderId) {
        return orderId != null && cancelledOrders.contains(orderId);
    }
    /**
     * （可选）移除取消标记，防止内存泄漏。
     * @param orderId 要移除标记的订单ID
     */
    private void removeCancellationMark(Long orderId) {
        if (orderId != null) {
            cancelledOrders.remove(orderId);
            log.debug("Cancellation mark removed for Order ID [{}]", orderId);
        }
    }
    /**
     * 异步执行一个模拟配送流程.
     * 使用 @Async 方法在一个独立的线程中运行, 不会阻塞RabbitMQ的监听线程.
     * 在每个阶段，它都会调用 DeliveryStatusPublisher
     * 将状态报告回 store-app。
     * @param deliveryRequest 包含订单信息的配送请求.
     */
    @Async
    public void simulateDelivery(DeliveryRequest deliveryRequest) {
        Long orderId = deliveryRequest.getOrderId();
        log.info("Order [{}]: Starting delivery simulation.", orderId);

        try {// 将整个模拟过程放入 try-finally 确保标记被移除
            // 模拟揽收前的检查
            if (isOrderCancelled(orderId)) {
                log.warn("Order [{}] was already cancelled before pickup simulation started. Aborting.", orderId);
                return; // 直接退出
            }
            TimeUnit.SECONDS.sleep(10); // 模拟揽收时间
            // 揽收后的检查（以防在sleep期间被取消）
            if (isOrderCancelled(orderId)) {
                log.warn("Order [{}] was cancelled during pickup simulation. Aborting.", orderId);
                return;
            }
            if(packageBeLost()){
                log.error("Order [{}]: Package LOST during pickup phase!", orderId);
                statusPublisher.sendStatusUpdate(orderId, STATUS_LOST);
                return; // 丢失后也直接退出
            }
            log.info("Order [{}]: Package has been picked up.", orderId);
            statusPublisher.sendStatusUpdate(orderId, STATUS_PICKED_UP);
            TimeUnit.SECONDS.sleep(5); // 模拟揽收到运输的过渡时间


            log.info("Order [{}]: Package is IN TRANSIT.", orderId);
            statusPublisher.sendStatusUpdate(orderId, STATUS_IN_TRANSIT);
            TimeUnit.SECONDS.sleep(10); // 模拟运输时间
            if (packageBeLost()) {
                log.error("Order [{}]: Package LOST during transit!", orderId);
                statusPublisher.sendStatusUpdate(orderId, STATUS_LOST);
                return; // 丢失后退出
            }

            log.info("Order [{}]: Package has been DELIVERED.", orderId);
            statusPublisher.sendStatusUpdate(orderId, STATUS_DELIVERED);


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order [{}]: Delivery simulation was interrupted.", orderId);
        } finally {
            // 无论模拟是正常完成、因取消而中止、还是因异常中断，
            // 最好都尝试移除取消标记，防止Set无限增大。
            removeCancellationMark(orderId);
        }
    }
        // 模拟 5% 的包裹丢失率
        private boolean packageBeLost() {
            // 生成一个 0-99 的随机数，如果小于5，则失败
            return random.nextInt(100) < 70;
        }
}
