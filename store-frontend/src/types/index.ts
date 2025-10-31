// User and Auth types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface User {
  id: number;
  username: string;
}

// Product types
export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl?: string;
  isActive: boolean;
}

export interface stock {
  productId: number;
  quantity: number;
  warehouseId: number;
}

// Order types
export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  AWAITING_SHIPMENT = 'AWAITING_SHIPMENT',
  SHIPPED = 'SHIPPED',
  IN_TRANSIT = 'IN_TRANSIT',
  REFUNDED = 'REFUNDED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

export interface OrderItem {
  productId: number;
  quantity: number;
  price: number;
  productName: string;
}

export interface CreateOrderRequest {
  userId: number;
  productId: number;
  quantity: number;
  deliveryAddress: string;
}

export interface OrderWarehouseAllocationDTO {
  id: number;
  warehouseId: number;
  warehouseName: string;
  allocatedQuantity: number;
}

export interface OrderItemDTO {
  id: number;
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  userId: number;
  username: string;
  orderDate: string;
  orderStatus: string;
  totalPrice: number;
  deliveryAddress: string;
  warehouseAllocations?: OrderWarehouseAllocationDTO[];
  orderItems?: OrderItemDTO[];
}

