import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const { isAuthenticated, logout } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navigation */}
      <nav className="bg-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-8">
              <Link to="/" className="text-xl font-bold text-blue-600">
                Store
              </Link>
              {isAuthenticated && (
                <Link
                  to="/products"
                  className="px-4 py-2 text-gray-700 hover:text-blue-600 rounded-md transition-colors"
                >
                  Products
                </Link>
              )}
            </div>
            <div className="flex items-center space-x-4">
              {isAuthenticated && (
                <Link
                  to="/orders"
                  className="px-4 py-2 text-gray-700 hover:text-blue-600 rounded-md transition-colors"
                >
                  Orders
                </Link>
              )}
              {isAuthenticated ? (
                <button
                  onClick={logout}
                  className="px-4 py-2 text-red-600 hover:bg-red-50 rounded-md"
                >
                  Logout
                </button>
              ) : (
                <Link
                  to="/login"
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  Login
                </Link>
              )}
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <p className="text-center text-gray-500 text-sm">
            © 2024 Store Application. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Layout;

