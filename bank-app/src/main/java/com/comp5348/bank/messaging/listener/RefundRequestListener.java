package com.comp5348.bank.messaging.listener;

import com.comp5348.bank.config.RabbitMQConfig;
import com.comp5348.bank.messaging.publisher.BankEventPublisher;
import com.comp5348.bank.service.BankService;
import com.comp5348.dto.RefundRequest;
import com.comp5348.dto.RefundResponse;
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
public class RefundRequestListener {

    private static final Logger log = LoggerFactory.getLogger(RefundRequestListener.class);

    @Autowired
    private BankService bankService;

    @Autowired
    private BankEventPublisher bankEventPublisher;

    // private static final String CUSTOMER_ACCOUNT_NUMBER = "CUST1001";
    private static final String STORE_ACCOUNT_NUMBER = "STORE001";  // 付款方 (商店)

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REFUND_REQUEST)
    @Transactional
    public void handleRefundRequest(RefundRequest request) {
        log.info(">>>> [BANK-APP] Received Refund Request for Order ID [{}], Amount: {}",
                request.getOrderId(), request.getAmount());

        if (request.getCustomerBankAccountNumber() == null || request.getCustomerBankAccountNumber().isEmpty()) {
            log.error("Received RefundRequest for Order ID [{}] with missing or empty customerBankAccountNumber. Rejecting message.", request.getOrderId());
            return;
        }

        TransferResponse bankResult;
        try {
            BigDecimal refundAmount = request.getAmount();
            Long orderIdLong = request.getOrderId();
            String customerAccount = request.getCustomerBankAccountNumber();

            if (refundAmount == null || orderIdLong == null) {
                log.error("Received RefundRequest with null amount or orderId for order {}", orderIdLong);
                bankResult = new TransferResponse(null, "FAILED", "Invalid refund request data");
            } else {

                TransferRequest bankTransferRequest = new TransferRequest();
                bankTransferRequest.setFromAccount(STORE_ACCOUNT_NUMBER);
                bankTransferRequest.setToAccount(customerAccount);
                bankTransferRequest.setAmount(refundAmount);
                bankTransferRequest.setOrderId(String.valueOf(orderIdLong));
                bankTransferRequest.setDescription("Refund for order " + orderIdLong);


                bankResult = bankService.processRefund(bankTransferRequest);
            }
        } catch (Exception e) {
            log.error("Error processing refund for order ID [{}]: {}", request.getOrderId(), e.getMessage(), e);
            bankResult = new TransferResponse(null, "FAILED", "Internal bank error: " + e.getMessage());
        }


        RefundResponse responseToStore = new RefundResponse();
        responseToStore.setOrderId(request.getOrderId());
        responseToStore.setRefundStatus(bankResult.getStatus());
        responseToStore.setRefundTransactionId(bankResult.getTransactionId());


        bankEventPublisher.sendRefundResponse(responseToStore);
    }
}