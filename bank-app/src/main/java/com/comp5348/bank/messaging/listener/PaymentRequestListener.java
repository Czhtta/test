package com.comp5348.bank.messaging.listener;

import com.comp5348.bank.config.RabbitMQConfig;
import com.comp5348.bank.messaging.publisher.BankEventPublisher;
import com.comp5348.bank.service.BankService;
import com.comp5348.dto.PaymentRequest;
import com.comp5348.dto.PaymentResponse;
import com.comp5348.bank.dto.TransferRequest;
import com.comp5348.bank.dto.TransferResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class PaymentRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestListener.class);

    @Autowired
    private BankService bankService;

    @Autowired
    private BankEventPublisher bankEventPublisher;


    // private static final String CUSTOMER_ACCOUNT_NUMBER = "CUST1001";
    private static final String STORE_ACCOUNT_NUMBER = "STORE001";  // 假设的商店账号

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_REQUEST)
    @Transactional
    public void handlePaymentRequest(PaymentRequest request) { // 接收来自 store-app 的 PaymentRequest
        log.info(">>>> [BANK-APP] Received Payment Request for Order ID [{}], Amount: {}",
                request.getOrderId(), request.getAmount());

        TransferResponse bankResult;
        try {
            BigDecimal paymentAmount = request.getAmount();
            Long orderIdLong = request.getOrderId();

            if (paymentAmount == null || orderIdLong == null) {
                log.error("Received PaymentRequest with null amount or orderId for order {}", orderIdLong);
                bankResult = new TransferResponse(null, "FAILED", "Invalid payment request data");
            } else {
                TransferRequest bankTransferRequest = new TransferRequest();
                bankTransferRequest.setFromAccount(request.getCustomerBankAccountNumber());// 付款方
                bankTransferRequest.setToAccount(STORE_ACCOUNT_NUMBER);     // 收款方
                bankTransferRequest.setAmount(paymentAmount);
                bankTransferRequest.setOrderId(String.valueOf(orderIdLong));
                bankTransferRequest.setDescription("Payment for order " + orderIdLong);

                bankResult = bankService.processPayment(bankTransferRequest);
            }
        } catch (Exception e) {
            log.error("Error processing payment for order ID [{}]: {}", request.getOrderId(), e.getMessage(), e);

            bankResult = new TransferResponse(null, "FAILED", "Internal bank error: " + e.getMessage());
        }


        PaymentResponse responseToStore = new PaymentResponse();
        responseToStore.setOrderId(request.getOrderId());
        responseToStore.setPaymentStatus(bankResult.getStatus());
        responseToStore.setTransactionId(bankResult.getTransactionId());


        bankEventPublisher.sendPaymentResponse(responseToStore);
    }
}