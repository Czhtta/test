package com.comp5348.store.messaging.listener;

import com.comp5348.dto.PaymentResponse;
import com.comp5348.store.config.RabbitMQConfig;
import com.comp5348.store.service.OrderProcessingService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PaymentResultListener {
    public static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    @Autowired
    private OrderProcessingService orderProcessingService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_RESPONSE)
    public void handlePaymentResult(
            PaymentResponse response,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {

        try {
            log.info(">>>> [STORE-APP] Received payment result for Order ID [{}]: {}", response.getOrderId(), response.getPaymentStatus());


            orderProcessingService.processPaymentResult(response);

            // 业务成功，手动确认消息
            log.debug("Successfully processed payment result for Order ID [{}]. Acknowledging message.", response.getOrderId());
            channel.basicAck(tag, false);

        } catch (Exception e) {
            // 业务失败 (事务已回滚)，手动拒绝消息
            log.error("Failed to process payment result for order {}. Transaction rolled back. Rejecting message.", response.getOrderId(), e);

            // false = 只拒绝这一条
            channel.basicNack(tag, false, false);
        }
    }
}