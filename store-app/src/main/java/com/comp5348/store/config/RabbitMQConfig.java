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
 * 注意因为这时候bank的通信还没写，所以后续如果你们写的话可能需要用我这个框架参照了。
 * 同时作为生产者和消费者
 * 生产者: 声明了需要将消息发送到的目标队列（如 email, delivery 请求队列）
 * 消费者: 声明了自身需要监听的队列（如 delivery status 更新队列）
 * 并创建了相应的绑定规则。
 */
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "comp5348.topic";

    public static final String QUEUE_EMAIL_REQUEST = "q.email.request";
    public static final String QUEUE_DELIVERY_REQUEST = "q.delivery.request";
    //public static final String QUEUE_PAYMENT_REQUEST = "q.payment.request";

    public static final String ROUTING_KEY_EMAIL = "email.request";
    public static final String ROUTING_KEY_DELIVERY = "delivery.request";
    //public static final String ROUTING_KEY_PAYMENT_REQUEST = "payment.request";
    public static final String QUEUE_DELIVERY_STATUS_UPDATE = "q.delivery.status.update";
    public static final String ROUTING_KEY_DELIVERY_STATUS_UPDATE = "delivery.status.update";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    // 确保即使消费者应用后启动，消息也不会丢失
    @Bean
    public Queue emailRequestQueue() {
        return new Queue(QUEUE_EMAIL_REQUEST);
    }
    @Bean
    public Binding bindingEmailRequestQueue(Queue emailRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailRequestQueue).to(exchange).with(ROUTING_KEY_EMAIL);
    }
    // 确保即使消费者应用后启动，消息也不会丢失
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
}
