import React from 'react';
import { Link } from 'react-router-dom';

const Home: React.FC = () => {
  return (
    <div className="text-center py-16">
      <h1 className="text-5xl font-bold mb-6 text-blue-600">
        Welcome to Our Store
      </h1>
      <p className="text-xl text-gray-600 mb-8">
        Please login to access the system
      </p>
      <div>
        <Link
          to="/login"
          className="inline-block bg-blue-600 text-white px-8 py-3 rounded-md hover:bg-blue-700 transition-colors"
        >
          Login
        </Link>
      </div>
      <br />
      <div>
        <Link
          to="/products"
          className="inline-block bg-blue-600 text-white px-8 py-3 rounded-md hover:bg-blue-700 transition-colors"
        >
          Products
        </Link>
      </div>
      <br />
    </div>
  );
};

export default Home;

