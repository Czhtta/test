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
public class RefundRequestListener {

    private static final Logger log = LoggerFactory.getLogger(RefundRequestListener.class);

    private final BankService bankService;
    private final RabbitTemplate rabbitTemplate;

    public RefundRequestListener(BankService bankService, RabbitTemplate rabbitTemplate) {
        this.bankService = bankService;
        this.rabbitTemplate = rabbitTemplate;
    }

    // 监听 store 发来的退款请求
    @RabbitListener(queues = RabbitMQConfig.QUEUE_REFUND_REQUEST)
    public void handleRefundRequest(TransferRequest request) {
        log.info("🔄 Received refund request: orderId={}, from={}, to={}, amount={}",
                request.getOrderId(), request.getFromAccount(), request.getToAccount(), request.getAmount());
        try {
            TransferResponse resp = bankService.processRefund(request);
            log.info("✅ Refund processed: orderId={}, status={}", request.getOrderId(), resp.getStatus());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_PAYMENT_STATUS,
                    resp
            );
        } catch (Exception e) {
            log.error("❌ Failed to process refund: orderId={}, error={}", request.getOrderId(), e.getMessage(), e);
            TransferResponse fail = new TransferResponse(null, "FAILED",
                    "Error processing refund: " + e.getMessage());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_PAYMENT_STATUS,
                    fail
            );
        }
    }
}
