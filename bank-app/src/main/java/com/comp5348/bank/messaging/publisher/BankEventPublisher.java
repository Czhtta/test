package com.comp5348.bank.messaging.publisher;


import com.comp5348.bank.config.RabbitMQConfig;
import com.comp5348.dto.PaymentResponse;
import com.comp5348.dto.RefundRequest;
import com.comp5348.dto.RefundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BankEventPublisher {
    public static final Logger log = LoggerFactory.getLogger(BankEventPublisher.class);

    // 这个处理器会将任何消息的投递模式设置为 PERSISTENT (持久化)
    private static final MessagePostProcessor PERSISTENT_MESSAGE_PROCESSOR = new MessagePostProcessor() {
        @Override
        public Message postProcessMessage(Message message) throws AmqpException {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        }
    };

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendPaymentResponse(PaymentResponse response) {
        log.info(">>>> [BANK-APP] Sending payment response for Order ID [{}]: {}",
                response.getOrderId(), response.getPaymentStatus());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_PAYMENT_RESPONSE,
                response,
                PERSISTENT_MESSAGE_PROCESSOR);
    }
    public void sendRefundResponse(RefundResponse response) {
        log.info(">>>> [BANK-APP] Sending refund response for Order ID [{}]: {}",
                response.getOrderId(), response.getRefundStatus());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_REFUND_RESPONSE,
                response,
                PERSISTENT_MESSAGE_PROCESSOR);
    }
}
