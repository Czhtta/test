package com.comp5348.store.messaging.listener;

import com.comp5348.dto.EmailRequest;
import com.comp5348.dto.RefundResponse;
import com.comp5348.store.entity.Order;
import com.comp5348.store.entity.OrderStatus;
import com.comp5348.store.messaging.publisher.OrderEventPublisher;
import com.comp5348.store.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefundResponseListener {
    public static final Logger log = LoggerFactory.getLogger(RefundResponseListener.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    public void handleRefundResult(RefundResponse response) {
        log.info(">>>> [STORE-APP] Received refund result for Order ID [{}]: {}",
                response.getOrderId(), response.getRefundStatus());
        Order order = orderRepository.findById(response.getOrderId())
                .orElse(null);
        if(order == null){
            log.error("Received refund result for unknown order ID: {}", response.getOrderId());
            return;
        }
        String userEmail = order.getUser().getEmail();
        String subject;
        String body;

        if("SUCCESS".equals(response.getRefundStatus())){
            order.setOrderStatus(OrderStatus.REFUNDED);
            subject = "Your refund is Complete for Order" + order.getId();
            body = "Your refund for order (ID" + order.getId() + ") has been processed successfully."
            + "Transaction ID: " + response.getRefundTransactionId();
        }else{
            log.error("Refund failed for Order ID [{}]", order.getId());
            subject = "Refund Failed for Order" + order.getId();
            body = "We encountered an issue while processing your refund for order (ID" + order.getId() + ")."
            + "Please contact customer support for assistance.";
        }
        orderRepository.save(order);

        EmailRequest email = new EmailRequest();
        email.setTo(userEmail);
        email.setSubject(subject);
        email.setBody(body);
        orderEventPublisher.sendEmailRequest(email);
    }
}
