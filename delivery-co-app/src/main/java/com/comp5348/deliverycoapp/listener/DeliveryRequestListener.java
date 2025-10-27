package com.comp5348.deliverycoapp.listener;

import com.comp5348.deliverycoapp.config.RabbitMQConfig;
import com.comp5348.deliverycoapp.service.SimulationService;
import com.comp5348.dto.DeliveryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消费者，监听并处理 store-app 的发货请求。
 * 收到发货请求后，不会自己执行耗时的模拟任务，而是委托给
 * 异步的 SimulationService
 * 避免阻塞 RabbitMQ 的监听线程
 */
@Component
public class DeliveryRequestListener {
    public static final Logger log = LoggerFactory.getLogger(DeliveryRequestListener.class);

    @Autowired
    private SimulationService simulationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DELIVERY_REQUEST)
    public void handleDeliveryRequest(DeliveryRequest deliveryRequest) {
        log.info(">>>>> Received Delivery request for order ID: {}", deliveryRequest.getOrderId());
        simulationService.simulateDelivery(deliveryRequest);
    }
}
