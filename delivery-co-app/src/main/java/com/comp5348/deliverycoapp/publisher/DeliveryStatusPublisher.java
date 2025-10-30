package com.comp5348.deliverycoapp.publisher;

import com.comp5348.dto.DeliveryStatusUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
/**
 * 生产者，将配送状态的更新作为消息发送回 store-app。
 * 构建 DeliveryStatusUpdate 对象，通过 RabbitMQ发送到中央交换机
 * 使用特定路由键，确保消息能被路由到 store-app 的监听队列中
 */
@Component
public class DeliveryStatusPublisher {
    public static final Logger log = LoggerFactory.getLogger(DeliveryStatusPublisher.class);

    // 这个处理器会将任何消息的投递模式设置为 PERSISTENT (持久化)
    private static final MessagePostProcessor PERSISTENT_MESSAGE_PROCESSOR = new MessagePostProcessor() {
        @Override
        public Message postProcessMessage(Message message) throws AmqpException {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        }
    };
    public static final String EXCHANGE_NAME = "comp5348.topic";
    public static final String ROUTING_KEY_DELIVERY_STATUS_UPDATE = "delivery.status.update";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendStatusUpdate(Long orderId, String status) {
        DeliveryStatusUpdate statusUpdate = new DeliveryStatusUpdate();
        statusUpdate.setOrderId(orderId);
        statusUpdate.setDeliveryStatus(status);
        statusUpdate.setUpdateTime(LocalDateTime.now());

        log.info("<<<<<< [DELIVERY-CO-APP] Sending delivery status update for Order ID [{}]: {}",
                orderId, status);

        rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                ROUTING_KEY_DELIVERY_STATUS_UPDATE,
                statusUpdate,
                PERSISTENT_MESSAGE_PROCESSOR);
    }
}
