import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService } from '../services/productService';
import { orderService } from '../services/orderService';
import api from '../services/api';
import { Product } from '../types';

const ProductDetail: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState<number>(1);
  const [shippingAddress, setShippingAddress] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [totalStock, setTotalStock] = useState<number | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const data = await productService.getProductById(Number(id));
        setProduct(data);
        // fetch total stock
        const stockRes = await productService.getProductTotalStock(Number(id));
        setTotalStock(stockRes);
      } catch (e) {
        setError('Failed to load product');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!product) return;
    if (!shippingAddress.trim()) {
      alert('Please enter shipping address');
      return;
    }
    if (quantity <= 0) {
      alert('Quantity must be greater than 0');
      return;
    }
    try {
      setIsSubmitting(true);
      await orderService.createOrder({
        userId: 1,
        productId: product.id,
        quantity,
        shippingAddress: shippingAddress.trim(),
        paymentMethod: 'credit_card',
      });
      alert('Order placed successfully!');
      navigate('/products');
    } catch (err) {
      console.error(err);
      alert('Failed to place order');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl">Loading...</div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="max-w-3xl mx-auto p-6">
        <p className="text-red-600">{error || 'Product not found'}</p>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto p-6">
      <button onClick={() => navigate(-1)} className="mb-4 text-blue-600 hover:underline">← Back</button>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 bg-white rounded-lg shadow p-6">
        <div className="bg-gray-100 flex items-center justify-center p-4 rounded">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="max-w-full max-h-[480px] object-contain rounded-md" />
          ) : (
            <span className="text-gray-400">No Image</span>
          )}
        </div>
        <div>
          <h1 className="text-2xl font-bold mb-2">{product.name}</h1>
          <p className="text-gray-600 mb-4">{product.description}</p>
          <p className="text-sm text-gray-500 mb-2">Category: {product.category}</p>
          {totalStock !== null && (
            <p className={`text-sm mb-4 ${totalStock > 0 ? 'text-green-600' : 'text-red-600'}`}>Available: {totalStock}</p>
          )}
          <p className="text-2xl font-bold text-blue-600 mb-6">${product.price.toFixed(2)}</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
              <input
                type="number"
                min={1}
                max={totalStock ?? undefined}
                value={quantity}
                onChange={(e) => {
                  const raw = parseInt(e.target.value || '1', 10);
                  const minVal = 1;
                  const maxVal = totalStock !== null ? Math.max(totalStock, 0) : undefined;
                  let next = Number.isNaN(raw) ? minVal : raw;
                  if (maxVal !== undefined) next = Math.min(next, maxVal);
                  next = Math.max(next, minVal);
                  setQuantity(next);
                }}
                className="w-full md:w-40 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              {totalStock !== null && quantity > totalStock && (
                <p className="mt-1 text-xs text-red-600">Quantity exceeds available stock</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Shipping Address</label>
              <textarea
                value={shippingAddress}
                onChange={(e) => setShippingAddress(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={3}
              />
            </div>
            <button
              type="submit"
              disabled={isSubmitting || totalStock === 0 || (totalStock !== null && quantity > totalStock)}
              className="w-full md:w-auto px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400"
            >
              {isSubmitting ? 'Placing...' : 'Place Order'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;


