import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService } from '../services/productService';
import { orderService } from '../services/orderService';
import { Product } from '../types';
import { useAuth } from '../context/AuthContext';

const ProductDetail: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
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
    if (!isAuthenticated) {
      // 跳到登录页，并带上当前页地址（可选：回跳）
      navigate('/login');
      return;
    }
    if (!product || !user) return;
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
        userId: user.id,
        productId: product.id,
        quantity,
        deliveryAddress: shippingAddress.trim(), // 字段更名
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
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 via-white to-white">
        <div className="text-xl text-gray-700">Loading product…</div>
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

  const increment = () => {
    const next = totalStock !== null ? Math.min(quantity + 1, Math.max(totalStock, 1)) : quantity + 1;
    setQuantity(next);
  };
  const decrement = () => {
    const next = Math.max(quantity - 1, 1);
    setQuantity(next);
  };

  return (
    <div className="bg-gradient-to-b from-blue-50/40 via-white to-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button onClick={() => navigate(-1)} className="mb-6 inline-flex items-center text-blue-700 hover:text-blue-800">
          <span className="mr-1">←</span> Back
        </button>

        {/* Header */}
        <div className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight text-gray-900">{product.name}</h1>
            <span className="text-xs px-2 py-1 rounded-full bg-blue-600 text-white">{product.category}</span>
          </div>
          <p className="text-gray-600 max-w-3xl">{product.description}</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left: image and details */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4 md:p-6">
              <div className="aspect-square bg-white rounded-lg border border-gray-100 flex items-center justify-center w-full mx-auto md:max-w-[380px]">
                {product.imageUrl ? (
                  <img src={product.imageUrl} alt={product.name} className="w-full h-full object-contain p-6" />
                ) : (
                  <span className="text-gray-400">No Image</span>
                )}
              </div>
            </div>

            
          </div>

          {/* Right: sticky action card */}
          <div className="lg:col-span-1">
            <div className="sticky top-6 bg-white rounded-xl border border-gray-100 shadow-sm p-6">
              <div className="flex items-center justify-between mb-3">
                <p className="text-2xl font-bold text-blue-700">${product.price.toFixed(2)}</p>
                {totalStock !== null && (
                  <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                    totalStock > 0 ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                  }`}>
                    {totalStock > 0 ? `In stock: ${totalStock}` : 'Out of stock'}
                  </span>
                )}
              </div>

              <div className="space-y-5">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
                  <div className="flex items-center w-full sm:w-48">
                    <button type="button" onClick={decrement} className="px-3 py-2 border border-r-0 border-gray-300 rounded-l-md hover:bg-gray-50">-</button>
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
                      className="w-full px-3 py-2 border border-gray-300 text-center focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button type="button" onClick={increment} className="px-3 py-2 border border-l-0 border-gray-300 rounded-r-md hover:bg-gray-50">+</button>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Shipping Address</label>
                  <textarea
                    value={shippingAddress}
                    onChange={(e) => setShippingAddress(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    rows={4}
                  />
                </div>

                <button
                  type="button"
                  onClick={handleSubmit as unknown as () => void}
                  disabled={isSubmitting || totalStock === 0 || (totalStock !== null && quantity > totalStock)}
                  className="w-full inline-flex items-center justify-center px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400"
                >
                  {isSubmitting ? 'Placing…' : 'Place Order'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;


