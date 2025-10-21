package com.comp5348.emailapp.config;

import com.comp5348.emailapp.listener.EmailRequestListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_EMAIL_REQUEST = "q.email.request";
    /**
     * 接收邮件发送请求的队列.
     * 这个Bean会确保在RabbitMQ服务器上存在一个名为 "q.email.request" 的持久化队列.
     */
    @Bean
    public Queue emailRequestQueue() {
        return new Queue(QUEUE_EMAIL_REQUEST);
    }
    /**
     * 消息转换器, 将Java对象序列化为JSON格式, 以及反序列化.
     * 确保所有微服务都使用同一种转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

//    先注释掉，跑不通再用
//    @Bean
//    MessageListenerAdapter listenerAdapter(EmailRequestListener receiver, MessageConverter messageConverter) {
//        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver,
//                                                "handleEmailRequest");
//        adapter.setMessageConverter(messageConverter);
//        return adapter;
//    }
//
//    @Bean
//    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
//                                             MessageListenerAdapter listenerAdapter) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames(QUEUE_EMAIL_REQUEST);
//        container.setMessageListener(listenerAdapter);
//        return container;
//    }

}
