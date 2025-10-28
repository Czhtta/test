package com.comp5348.emailapp.listener;

import com.comp5348.dto.EmailRequest;
import com.comp5348.emailapp.config.RabbitMQConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class EmailRequestListener {
    public static final Logger log = LoggerFactory.getLogger(EmailRequestListener.class);
    /**
     * 监听 "q.email.request" 队列.
     * 当有新消息到达时, Spring AMQP会将其从JSON反序列化为EmailRequest对象,
     * 并调用此方法进行处理.
     * @param emailRequest 从队列中收到的邮件发送请求对象.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL_REQUEST)
    public void  handleEmailRequest(EmailRequest emailRequest) {
        log.info(">>>> Received Email request: {}", emailRequest);

        // 根据项目要求, 模拟邮件发送过程, 仅在控制台打印信息
        System.out.println("======================================================");
        System.out.println("Sending email...");
        System.out.println("To: " + emailRequest.getTo());
        System.out.println("Subject: " + emailRequest.getSubject());
        System.out.println("Body: " + emailRequest.getBody());
        System.out.println("Email sent successfully!");
        System.out.println("======================================================");
    }
}
