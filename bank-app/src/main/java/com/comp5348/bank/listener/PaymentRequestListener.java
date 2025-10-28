package com.comp5348.bank.listener;

import com.comp5348.bank.config.RabbitMQConfig;
import com.comp5348.bank.dto.TransferRequest;
import com.comp5348.bank.dto.TransferResponse;
import com.comp5348.bank.service.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestListener.class);

    private final BankService bankService;
    private final RabbitTemplate rabbitTemplate;

    public PaymentRequestListener(BankService bankService, RabbitTemplate rabbitTemplate) {
        this.bankService = bankService;
        this.rabbitTemplate = rabbitTemplate;
    }

    // 监听 store 发来的支付请求
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_REQUEST)
    public void handlePaymentRequest(TransferRequest request) {
        log.info("💰 Received payment request: orderId={}, from={}, to={}, amount={}",
                request.getOrderId(), request.getFromAccount(), request.getToAccount(), request.getAmount());
        try {
            TransferResponse response = bankService.processPayment(request);
            log.info("✅ Payment processed: orderId={}, status={}", request.getOrderId(), response.getStatus());

            // 回传异步状态到 store
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_PAYMENT_STATUS,
                    response
            );
        } catch (Exception e) {
            log.error("❌ Failed to process payment: orderId={}, error={}", request.getOrderId(), e.getMessage(), e);
            TransferResponse fail = new TransferResponse(null, "FAILED",
                    "Error processing payment: " + e.getMessage());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_PAYMENT_STATUS,
                    fail
            );
        }
    }
}
