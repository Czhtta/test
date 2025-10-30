package com.comp5348.store.service;

import com.comp5348.dto.PaymentResponse;

/**
 * 此服务封装了处理支付结果所需的所有业务逻辑和事务。
 * 将其与 RabbitMQ 监听器分离，以实现清晰的职责分离和正确的事务管理。
 */
public interface OrderProcessingService {

    /**
     * 处理支付结果的业务逻辑。
     * 此方法旨在 @Transactional 中运行。
     * @param response 从 bank-app 收到的支付响应
     * @throws Exception 抛出任何异常以触发事务回滚
     */
    void processPaymentResult(PaymentResponse response) throws Exception;
}