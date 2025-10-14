package com.comp5348.store.entity;

public enum OrderStatus {
    PENDING,            // 待处理 (刚创建)
    PAYMENT_PROCESSING, // 支付处理中
    PAYMENT_SUCCESS,    // 支付成功
    PAYMENT_FAILED,     // 支付失败
    AWAITING_SHIPMENT,  // 等待发货
    SHIPPED,            // 已发货
    IN_TRANSIT,         // 运输中
    DELIVERED,          // 已送达
    CANCELLED,          // 已取消
    REFUND_PROCESSING,  // 退款处理中
    REFUNDED            // 已退款
}
