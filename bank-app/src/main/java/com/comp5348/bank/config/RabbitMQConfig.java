package com.comp5348.bank.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "comp5348.topic";

    public static final String QUEUE_PAYMENT_REQUEST = "q.payment.request";
    public static final String ROUTING_KEY_PAYMENT_REQUEST = "payment.request";

    public static final String QUEUE_REFUND_REQUEST = "q.refund.request";
    public static final String ROUTING_KEY_REFUND_REQUEST = "refund.request";

    public static final String QUEUE_PAYMENT_STATUS = "q.payment.status";
    public static final String ROUTING_KEY_PAYMENT_STATUS = "payment.status";
    public static final String ROUTING_KEY_PAYMENT_RESPONSE = "payment.response";
    public static final String ROUTING_KEY_REFUND_RESPONSE = "refund.response";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(QUEUE_PAYMENT_REQUEST, true);
    }

    @Bean
    public Queue refundRequestQueue() {
        return new Queue(QUEUE_REFUND_REQUEST, true);
    }

    @Bean
    public Queue paymentStatusQueue() {
        return new Queue(QUEUE_PAYMENT_STATUS, true);
    }

    @Bean
    public Binding bindPaymentRequest(Queue paymentRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentRequestQueue).to(exchange).with(ROUTING_KEY_PAYMENT_REQUEST);
    }

    @Bean
    public Binding bindRefundRequest(Queue refundRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(refundRequestQueue).to(exchange).with(ROUTING_KEY_REFUND_REQUEST);
    }

    @Bean
    public Binding bindPaymentStatus(Queue paymentStatusQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentStatusQueue).to(exchange).with(ROUTING_KEY_PAYMENT_STATUS);
    }
}
