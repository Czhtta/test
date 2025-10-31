import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Home: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="relative overflow-hidden bg-gradient-to-b from-blue-50 via-white to-white">
      {/* Hero Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-28">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-10 items-center">
          <div>
            <span className="inline-block text-sm font-medium text-blue-700 bg-blue-100 px-3 py-1 rounded-full mb-4">
              Trusted E‑Commerce Platform
            </span>
            <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight text-gray-900 mb-4">
              Discover Quality Products at Great Prices
            </h1>
            <p className="text-gray-600 text-lg md:text-xl mb-8">
              {isAuthenticated
                ? 'Welcome back! Manage your orders or continue shopping our latest collection.'
                : 'Login to access your account and start shopping our curated selection.'}
            </p>

            <div className="flex flex-col sm:flex-row gap-4">
              {isAuthenticated ? (
                <Link
                  to="/orders"
                  className="inline-flex justify-center items-center px-6 py-3 rounded-lg text-white bg-blue-600 hover:bg-blue-700 shadow-sm transition-colors"
                >
                  View Orders
                </Link>
              ) : (
                <Link
                  to="/login"
                  className="inline-flex justify-center items-center px-6 py-3 rounded-lg text-white bg-blue-600 hover:bg-blue-700 shadow-sm transition-colors"
                >
                  Login
                </Link>
              )}

              <Link
                to="/products"
                className="inline-flex justify-center items-center px-6 py-3 rounded-lg text-blue-700 bg-blue-100 hover:bg-blue-200 transition-colors"
              >
                Browse Products
              </Link>
            </div>
          </div>

          {/* Illustration / Promo Card */}
          <div className="relative">
            <div className="rounded-2xl bg-white/70 backdrop-blur shadow-lg p-6 md:p-8 border border-gray-100">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-semibold text-gray-800">Highlights</h3>
                <span className="text-xs uppercase tracking-wide text-red-700 bg-red-100 px-2 py-1 rounded">Hot</span>
              </div>
              <ul className="space-y-4">
                <li className="flex items-start gap-3">
                  <span className="h-2.5 w-2.5 mt-2 rounded-full bg-blue-600" />
                  <div>
                    <p className="font-medium text-gray-900">Multiple warehouses</p>
                    <p className="text-sm text-gray-600">We have multiple warehouses across the world</p>
                  </div>
                </li>
                <li className="flex items-start gap-3">
                  <span className="h-2.5 w-2.5 mt-2 rounded-full bg-emerald-500" />
                  <div>
                    <p className="font-medium text-gray-900">Fast, reliable shipping</p>
                    <p className="text-sm text-gray-600">Track your packages in real time</p>
                  </div>
                </li>
                <li className="flex items-start gap-3">
                  <span className="h-2.5 w-2.5 mt-2 rounded-full bg-amber-500" />
                  <div>
                    <p className="font-medium text-gray-900">Secure payments</p>
                    <p className="text-sm text-gray-600">Make transactions securely and reliably</p>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Curated Selection</h4>
            <p className="text-gray-600">Hand‑picked products from trusted brands and sellers.</p>
          </div>
          <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Customer Support</h4>
            <p className="text-gray-600">We’re here to help with orders, returns, and more.</p>
          </div>
          <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Easy Returns</h4>
            <p className="text-gray-600">Hassle‑free returns within our policy window.</p>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;

