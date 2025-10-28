import api from './api';
import { Product } from '../types';

export const productService = {
  getAllProducts: async (): Promise<Product[]> => {
    const response = await api.get('/products');
    return response.data;
  },

  getProductById: async (id: number): Promise<Product> => {
    const response = await api.get(`/products/${id}`);
    return response.data;
  },

  getProductsByCategory: async (category: string): Promise<Product[]> => {
    const response = await api.get(`/products/category/${category}`);
    return response.data;
  },

  searchProducts: async (keyword: string): Promise<Product[]> => {
    const response = await api.get(`/products/search?keyword=${encodeURIComponent(keyword)}`);
    return response.data;
  },

  getAllCategories: async (): Promise<string[]> => {
    const response = await api.get('/products/categories');
    return response.data;
  },

  checkProductExists: async (id: number): Promise<boolean> => {
    const response = await api.get(`/products/${id}/exists`);
    return response.data;
  },
  
  getProductTotalStock: async (id: number): Promise<number> => {
    const response = await api.get(`/products/${id}/stock`);
    return response.data;
  },
};

