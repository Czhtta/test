import api from './api';
import { Order, CreateOrderRequest, OrderStatus } from '../types';

export const orderService = {
  createOrder: async (orderData: CreateOrderRequest): Promise<Order> => {
    const response = await api.post('/orders', orderData);
    return response.data;
  },

  getOrderById: async (orderId: number): Promise<Order> => {
    const response = await api.get(`/orders/${orderId}`);
    return response.data;
  },

  getOrdersByUserId: async (userId: number): Promise<Order[]> => {
    const response = await api.get(`/orders/user/${userId}`);
    return response.data;
  },

  getAllOrders: async (): Promise<Order[]> => {
    const response = await api.get('/orders');
    return response.data;
  },

  getOrdersByStatus: async (status: OrderStatus): Promise<Order[]> => {
    const response = await api.get(`/orders/status/${status}`);
    return response.data;
  },

  updateOrderStatus: async (orderId: number, status: OrderStatus): Promise<Order> => {
    const response = await api.put(`/orders/${orderId}/status?status=${status}`);
    return response.data;
  },

  cancelOrder: async (orderId: number): Promise<Order> => {
    const response = await api.put(`/orders/${orderId}/cancel`);
    return response.data;
  },
};

