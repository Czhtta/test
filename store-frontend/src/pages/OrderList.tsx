import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { orderService } from '../services/orderService';
import { Order } from '../types';
import { useNavigate } from 'react-router-dom';

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  SHIPPED: 'bg-purple-100 text-purple-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-gray-200 text-gray-700',
  AWAITING_SHIPMENT: 'bg-orange-100 text-orange-800',
  IN_TRANSIT: 'bg-cyan-100 text-cyan-800',
  REFUNDED: 'bg-gray-100 text-gray-500',
};

const NON_CANCELLABLE = new Set<string>(['CANCELLED', 'DELIVERED', 'SHIPPED', 'IN_TRANSIT', 'REFUNDED']);

const OrderList: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchOrders = async () => {
      if (!user) return;
      try {
        setLoading(true);
        const result = await orderService.getOrdersByUserId(user.id);
        setOrders(result);
      } finally {
        setLoading(false);
      }
    };
    if (isAuthenticated && user) {
      fetchOrders();
    }
  }, [isAuthenticated, user]);

  const handleCancel = async (orderId: number) => {
    const target = orders.find(o => o.id === orderId);
    if (!target) return;
    if (NON_CANCELLABLE.has(String(target.orderStatus))) return;
    const ok = window.confirm(`Cancel order #${orderId}?`);
    if (!ok) return;

    const retry = async (fn: () => Promise<void>, attempts = 3, baseDelayMs = 600) => {
      let lastErr: unknown = null;
      for (let i = 0; i < attempts; i++) {
        try {
          await fn();
          return;
        } catch (e) {
          lastErr = e;
          // 指数退避：0.6s, 1.2s, 2.4s（可快速跨过异步状态窗口）
          const delay = baseDelayMs * Math.pow(2, i);
          await new Promise(res => setTimeout(res, delay));
        }
      }
      throw lastErr;
    };

    try {
      setCancellingId(orderId);
      await retry(async () => {
        await orderService.cancelOrder(orderId);
      });
      setOrders(prev => prev.map(o => (o.id === orderId ? { ...o, orderStatus: 'CANCELLED' } : o)));
    } catch (e) {
      alert('Failed to cancel order. Please try again shortly.');
    } finally {
      setCancellingId(null);
    }
  };

  if (!isAuthenticated) return <div className="p-8 text-center text-lg">Please log in to view your orders.</div>;
  if (loading) return <div className="p-8 text-center text-lg">Loading...</div>;

  return (
    <div className="max-w-5xl mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl sm:text-3xl font-black ">My Orders</h2>
      </div>
      <div className="bg-white rounded-2xl shadow-md p-4 md:p-8">
        {orders.length === 0 ? (
          <div className="py-16 text-gray-500 text-center text-lg">No orders yet.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full border rounded-lg">
              <thead>
                <tr className="bg-gray-50 border-b">
                  <th className="px-4 py-2 text-left font-semibold">Order ID</th>
                  <th className="px-4 py-2 text-left font-semibold">Status</th>
                  <th className="px-4 py-2 text-left font-semibold">Total</th>
                  <th className="px-4 py-2 text-left font-semibold">Date</th>
                  <th className="px-4 py-2 text-left font-semibold">Address</th>
                  <th className="px-4 py-2"></th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => {
                  const disabled = NON_CANCELLABLE.has(String(order.orderStatus));
                  const isBusy = cancellingId === order.id;
                  return (
                    <tr key={order.id} className="border-t hover:bg-blue-50 group transition">
                      <td className="px-4 py-3 font-bold">{order.id}</td>
                      <td className="px-4 py-3">
                        <span className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${STATUS_COLORS[order.orderStatus] || 'bg-gray-200 text-gray-600'}`}>
                          {order.orderStatus}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-blue-700">${Number(order.totalPrice).toFixed(2)}</td>
                      <td className="px-4 py-3">{order.orderDate ? order.orderDate.split('T')[0] : ''}</td>
                      <td className="px-4 py-3 truncate max-w-xs">{order.deliveryAddress}</td>
                      <td className="px-4 py-3 flex gap-2 justify-end">
                        <button
                          className="px-4 py-1 bg-blue-500 hover:bg-blue-700 text-white rounded-full font-semibold transition shadow-sm hover:shadow"
                          onClick={() => navigate(`/order/${order.id}`)}
                        >
                          Details
                        </button>
                        <button
                          disabled={disabled || isBusy}
                          className={`px-4 py-1 rounded-full font-semibold transition shadow-sm border ${disabled || isBusy ? 'text-gray-400 border-gray-300 bg-gray-100 cursor-not-allowed' : 'text-red-600 border-red-300 hover:bg-red-50'}`}
                          onClick={() => handleCancel(order.id)}
                          title={disabled ? 'This order cannot be cancelled' : 'Cancel this order'}
                        >
                          {isBusy ? 'Cancelling...' : 'Cancel'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderList;
