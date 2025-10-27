package com.comp5348.store.messaging.listener;

import com.comp5348.dto.DeliveryStatusUpdate;
import com.comp5348.store.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
/**
 * 消费者，监听并处理 delivery-co-app 的配送状态更新。
 * 并将 JSON 反序列化为这里定义的dto对象：com.comp5348.dto.DeliveryStatusUpdate
 */
@Component
public class DeliveryStatusListener {
    public static final Logger log = LoggerFactory.getLogger(DeliveryStatusListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DELIVERY_STATUS_UPDATE)
    public void handleDeliveryStatus(DeliveryStatusUpdate statusUpdate) {
        log.info(">>>> [STORE-APP] Received delivery status update for Order ID [{}]: {}",
                statusUpdate.getOrderId(), statusUpdate.getDeliveryStatus());

        // 未来调用 OrderService 来更新数据库中的订单状态
        // 目前我只打印日志来验证接收成功
    }
}
