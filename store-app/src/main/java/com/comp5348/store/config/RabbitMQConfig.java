package com.comp5348.store.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * 负责声明所有 store-app 需要交互的交换机、队列和绑定。
 * 注意因为这时候bank的通信还没写，所以后续如果写的话可能需要用我这个框架参照了。
 */
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "comp5348.topic";

    public static final String QUEUE_EMAIL_REQUEST = "q.email.request";
    public static final String QUEUE_DELIVERY_REQUEST = "q.delivery.request";
    public static final String QUEUE_DELIVERY_STATUS_UPDATE = "q.delivery.status.update";

    public static final String ROUTING_KEY_EMAIL = "email.request";
    public static final String ROUTING_KEY_DELIVERY = "delivery.request";
    public static final String ROUTING_KEY_DELIVERY_STATUS_UPDATE = "delivery.status.update";

    public static final String QUEUE_PAYMENT_REQUEST = "q.payment.request";
    public static final String ROUTING_KEY_PAYMENT_REQUEST = "payment.request";

    public static final String QUEUE_PAYMENT_RESPONSE = "q.payment.response";
    public static final String ROUTING_KEY_PAYMENT_RESPONSE = "payment.response";

    public static final String QUEUE_REFUND_REQUEST = "q.refund.request";
    public static final String ROUTING_KEY_REFUND_REQUEST = "refund.request";
    public static final String QUEUE_REFUND_RESPONSE = "q.refund.response";
    public static final String ROUTING_KEY_REFUND_RESPONSE = "refund.response";

    public static final String QUEUE_DELIVERY_CANCELLATION = "q.delivery.cancellation";
    public static final String ROUTING_KEY_DELIVERY_CANCELLATION = "delivery.cancellation";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }


    @Bean
    public Queue emailRequestQueue() {
        return new Queue(QUEUE_EMAIL_REQUEST);
    }
    @Bean
    public Binding bindingEmailRequestQueue(Queue emailRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailRequestQueue).to(exchange).with(ROUTING_KEY_EMAIL);
    }
    @Bean
    public Queue deliveryRequestQueue() {
        return new Queue(QUEUE_DELIVERY_REQUEST);
    }
    @Bean
    public Binding bindingDeliveryRequestQueue(Queue deliveryRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deliveryRequestQueue).to(exchange).with(ROUTING_KEY_DELIVERY);
    }
    @Bean
    public Queue deliveryStatusUpdateQueue() {
        return new Queue(QUEUE_DELIVERY_STATUS_UPDATE);
    }
    @Bean
    public Binding bindingDeliveryStatusUpdateQueue(Queue deliveryStatusUpdateQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deliveryStatusUpdateQueue).to(exchange).with(ROUTING_KEY_DELIVERY_STATUS_UPDATE);
    }
    @Bean
    public Queue paymentRequestQueue(){ return new Queue(QUEUE_PAYMENT_REQUEST);}
    @Bean
    public Binding bindingPaymentRequestQueue(Queue paymentRequestQueue, TopicExchange exchange){
        return BindingBuilder.bind(paymentRequestQueue).to(exchange).with(ROUTING_KEY_PAYMENT_REQUEST);
    }
    @Bean
    public Queue paymentResponseQueue() { return new Queue(QUEUE_PAYMENT_RESPONSE);}
    @Bean
    public Binding bindingPaymentResponseQueue(Queue paymentResponseQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentResponseQueue).to(exchange).with(ROUTING_KEY_PAYMENT_RESPONSE);
    }
    @Bean
    public Queue refundRequestQueue() { return new Queue(QUEUE_REFUND_REQUEST); }
    @Bean
    public Binding bindingRefundRequestQueue(Queue refundRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(refundRequestQueue).to(exchange).with(ROUTING_KEY_REFUND_REQUEST);
    }
    @Bean
    public Queue refundResponseQueue() { return new Queue(QUEUE_REFUND_RESPONSE); }
    @Bean
    public Binding bindingRefundResponseQueue(Queue refundResponseQueue, TopicExchange exchange) {
        return BindingBuilder.bind(refundResponseQueue).to(exchange).with(ROUTING_KEY_REFUND_RESPONSE);
    }

    @Bean
    public Queue deliveryCancellationQueue() { return new Queue(QUEUE_DELIVERY_CANCELLATION); }
    @Bean
    public Binding bindingDeliveryCancellationQueue(Queue deliveryCancellationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deliveryCancellationQueue).to(exchange).with(ROUTING_KEY_DELIVERY_CANCELLATION);
    }
}
