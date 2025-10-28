// Application constants

export const API_BASE_URL = 'http://localhost:8080/api';

export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER: 'user',
} as const;

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  PRODUCTS: '/products',
  ORDERS: '/orders',
} as const;

export const ORDER_STATUS = {
  PENDING: 'PENDING',
  CONFIRMED: 'CONFIRMED',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
} as const;

