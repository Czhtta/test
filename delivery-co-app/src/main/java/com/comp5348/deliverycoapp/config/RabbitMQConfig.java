package com.comp5348.deliverycoapp.config;

import com.comp5348.deliverycoapp.listener.DeliveryRequestListener;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * 同时作为
 * 消费者，声明自身需要监听的发货请求队列 (q.delivery.request)
 * 生产者，也声明了需要发送消息到的状态更新队列
 * 绑定 q.delivery.status.update，确保消息总是有处可投，与store中配置作用一样
 */
@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_DELIVERY_REQUEST = "q.delivery.request";
    public static final String EXCHANGE_NAME = "comp5348.topic";

    public static final String QUEUE_DELIVERY_STATUS_UPDATE = "q.delivery.status.update";
    public static final String ROUTING_KEY_DELIVERY_STATUS_UPDATE = "delivery.status.update";

    public static final String QUEUE_DELIVERY_CANCELLATION = "q.delivery.cancellation";
    public static final String ROUTING_KEY_DELIVERY_CANCELLATION = "delivery.cancellation";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    @Bean
    public Queue deliveryRequestQueue() {
        return new Queue(QUEUE_DELIVERY_REQUEST);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
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
    public Queue deliveryCancellationQueue() { return new Queue(QUEUE_DELIVERY_CANCELLATION); }
    @Bean
    public Binding bindingDeliveryCancellationQueue(Queue deliveryCancellationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deliveryCancellationQueue).to(exchange).with(ROUTING_KEY_DELIVERY_CANCELLATION);
    }
//    先注释掉，跑不通会用这个。
//    @Bean
//    MessageListenerAdapter listenerAdapter(DeliveryRequestListener receiver, MessageConverter messageConverter) {
//        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "handleDeliveryRequest");
//        adapter.setMessageConverter(messageConverter);
//        return adapter;
//    }
//
//    @Bean
//    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
//                                             MessageListenerAdapter listenerAdapter) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames(QUEUE_DELIVERY_REQUEST);
//        container.setMessageListener(listenerAdapter);
//        return container;
//    }
}