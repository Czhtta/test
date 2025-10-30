import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { orderService } from '../services/orderService';
import { Order } from '../types';

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  SHIPPED: 'bg-purple-100 text-purple-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-gray-200 text-gray-700',
};

const OrderDetail: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchOrder = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const data = await orderService.getOrderById(Number(id));
        setOrder(data);
      } catch (err) {
        setError('Failed to fetch order details');
      } finally {
        setLoading(false);
      }
    };
    fetchOrder();
  }, [id]);

  if (loading) return <div className="p-6">Loading...</div>;
  if (error || !order) return <div className="p-6 text-red-600">{error || 'Order not found'}</div>;

  return (
    <div className="max-w-3xl mx-auto p-6">
      <button
        className="mb-4 text-blue-600 hover:underline font-medium flex items-center"
        onClick={() => navigate(-1)}
      >
        <span className="mr-2">&#8592;</span> Back
      </button>
      <div className="bg-white rounded-2xl shadow-md p-8 mb-10">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-2xl sm:text-3xl font-black">
            Order Detail <span className="font-extrabold text-gray-400">#{order.id}</span>
          </h2>
          <span className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${STATUS_COLORS[order.orderStatus] || 'bg-gray-200 text-gray-600'}`}>
            {order.orderStatus}
          </span>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-2 mb-2">
          <div>
            <span className="font-semibold text-gray-500">Order Date:</span>
            <span className="ml-2">{order.orderDate ? order.orderDate.split('T')[0] : '-'}</span>
          </div>
          <div>
            <span className="font-semibold text-gray-500">Delivery Address:</span>
            <span className="ml-2">{order.deliveryAddress}</span>
          </div>
          <div>
            <span className="font-semibold text-gray-500">Total:</span>
            <span className="ml-2 text-lg font-bold text-blue-600">${order.totalPrice.toFixed(2)}</span>
          </div>
          <div>
            <span className="font-semibold text-gray-500">Receiver:</span>
            <span className="ml-2">{order.username}</span>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="text-xl font-bold mb-4">Order Items</h3>
        <div className="overflow-x-auto">
          <table className="min-w-full border rounded-lg">
            <thead>
              <tr>
                <th className="px-4 py-2 border-b text-left">Product</th>
                <th className="px-4 py-2 border-b text-left">Unit Price</th>
                <th className="px-4 py-2 border-b text-left">Quantity</th>
                <th className="px-4 py-2 border-b text-left">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {order.orderItems?.map(item => (
                <tr key={item.id} className="border-t hover:bg-blue-50">
                  <td className="px-4 py-2">{item.productName}</td>
                  <td className="px-4 py-2">${Number(item.productPrice).toFixed(2)}</td>
                  <td className="px-4 py-2">{item.quantity}</td>
                  <td className="px-4 py-2">${Number(item.subtotal).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default OrderDetail;