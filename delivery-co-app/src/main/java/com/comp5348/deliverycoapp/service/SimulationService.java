package com.comp5348.deliverycoapp.service;

import com.comp5348.deliverycoapp.publisher.DeliveryStatusPublisher;
import com.comp5348.dto.DeliveryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SimulationService {
    public static final Logger log = LoggerFactory.getLogger(SimulationService.class);
    private final Random radom = new Random();
    private static final String STATUS_PICKED_UP = "PICKED_UP";
    private static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_LOST = "LOST";

    @Autowired
    private DeliveryStatusPublisher statusPublisher;
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
        log.info("Order [{}]: Starting delivery simulation.", deliveryRequest.getOrderId());
        try {
            // 模拟揽收
            TimeUnit.SECONDS.sleep(5);
            if(packageBeLost()){
                log.error("Order [{}]: Package LOST during pickup phase!", deliveryRequest.getOrderId());
                statusPublisher.sendStatusUpdate(orderId, STATUS_LOST);
                return;
            }

            log.info("Order [{}]: Package has benn picked up.", deliveryRequest.getOrderId());
            statusPublisher.sendStatusUpdate(orderId, STATUS_PICKED_UP);

            // 模拟运输
            TimeUnit.SECONDS.sleep(5);
            if (packageBeLost()) {
                log.error("Order [{}]: Package LOST during transit!", deliveryRequest.getOrderId());
                statusPublisher.sendStatusUpdate(orderId, STATUS_LOST);
                return;
            }

            log.info("Order [{}]: Package is IN TRANSIT.", deliveryRequest.getOrderId());
            statusPublisher.sendStatusUpdate(orderId, STATUS_IN_TRANSIT);

            // 模拟送达
            TimeUnit.SECONDS.sleep(5);
            log.info("Order [{}]: Package has been DELIVERED.", deliveryRequest.getOrderId());
            statusPublisher.sendStatusUpdate(orderId, STATUS_DELIVERED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order [{}]: Delivery simulation was interrupted.", deliveryRequest.getOrderId());
        }
    }
        // 模拟 5% 的包裹丢失率
        private boolean packageBeLost() {
            // 生成一个 0-99 的随机数，如果小于5，则失败
            return radom.nextInt(100) < 5;
        }
}
