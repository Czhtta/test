package com.comp5348.deliverycoapp.listener;
import com.comp5348.deliverycoapp.config.RabbitMQConfig;
import com.comp5348.deliverycoapp.service.SimulationService; // 假设检查/标记方法在这里
import com.comp5348.dto.DeliveryCancellationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeliveryCancellationListener {
    public static final Logger log = LoggerFactory.getLogger(DeliveryCancellationListener.class);

    @Autowired
    private SimulationService simulationService; // 或者管理 Future/Flag 的服务

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DELIVERY_CANCELLATION)
    public void handleCancellationRequest(DeliveryCancellationRequest request) {
        log.info(">>>>> Received Delivery Cancellation request for order ID: {}", request.getOrderId());
        // 在这里调用方法来标记或尝试中断对应的模拟任务
        simulationService.markOrderAsCancelled(request.getOrderId()); // 使用方法一的标记方式
        // 或者: simulationManager.cancelSimulation(request.getOrderId()); // 如果使用 Future 管理
    }
}