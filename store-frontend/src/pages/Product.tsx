import React, { useState, useEffect } from 'react';
import { productService } from '../services/productService';
import { Product } from '../types';
import { useNavigate } from 'react-router-dom';

const Products: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const handleProductDetail = (id: number) => {
    navigate(`/product/${id}`);
  };
  // Direct order flow: no cart, place order per product immediately

  useEffect(() => {
    loadProducts();
    loadCategories();
  }, []);

  useEffect(() => {
    filterProducts();
  }, [searchKeyword, selectedCategory, products]);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getAllProducts();
      // fetch total stock for each product and filter out zero-stock
      const stockList = await Promise.all(
        data.map(async (p) => ({ id: p.id, stock: await productService.getProductTotalStock(p.id) }))
      );
      const availableIds = new Set(stockList.filter((s) => (typeof s.stock === 'number' ? s.stock > 0 : true)).map((s) => s.id));
      const availableProducts = data.filter((p) => availableIds.has(p.id));
      setProducts(availableProducts);
    } catch (err) {
      setError('Failed to load products');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadCategories = async () => {
    try {
      const data = await productService.getAllCategories();
      setCategories(data);
    } catch (err) {
      console.error('Failed to load categories');
    }
  };

  const filterProducts = () => {
    let filtered = [...products];

    // Apply category filter
    if (selectedCategory !== 'all') {
      filtered = filtered.filter((product) => product.category === selectedCategory);
    }

    // Apply search filter
    if (searchKeyword) {
      filtered = filtered.filter(
        (product) =>
          product.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          product.description.toLowerCase().includes(searchKeyword.toLowerCase())
      );
    }

    setFilteredProducts(filtered);
  };


  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl">Loading...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Products</h1>
      </div>

      {error && <div className="mb-4 text-red-600">{error}</div>}

      {/* Search and Filter */}
      <div className="mb-6 flex flex-col md:flex-row gap-4">
        <input
          type="text"
          placeholder="Search products..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="all">All Categories</option>
          {categories.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
      </div>


      {/* Products Grid */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {filteredProducts.map((product) => (
          <div key={product.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
            <div
              className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow max-w-[260px] mx-auto"
            >
              {product.imageUrl ? ( 
                <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover p-2" />
              ) : (
                <span className="text-gray-400">No Image</span>
              )}
            </div>
            <div className="p-4">
              <h3 className="text-xl font-bold mb-2">{product.name}</h3>
              <p className="text-gray-600 mb-2 line-clamp-2">{product.description}</p>
              <p className="text-lg font-bold text-blue-600 mb-2">${product.price.toFixed(2)}</p>
              <button
                onClick={() => handleProductDetail(product.id)}
                className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
              >
                Product Detail
              </button>
            </div>
          </div>
        ))}
      </div>

      {filteredProducts.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-500 text-xl">No products found</p>
        </div>
      )}
    </div>
  );
};

export default Products;
