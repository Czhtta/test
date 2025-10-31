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
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 via-white to-white">
        <div className="text-xl text-gray-700">Loading products…</div>
      </div>
    );
  }

  return (
    <div className="bg-gradient-to-b from-blue-50/40 via-white to-white">
      {/* Header / Hero */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-10 pb-6">
        <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
          <div>
            <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight text-gray-900">
              Explore Products
            </h1>
            <p className="text-gray-600 mt-2">Find your next favorite item from our curated list.</p>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {error && <div className="mb-4 text-red-600">{error}</div>}

        {/* Search and Filter */}
        <div className="mb-8">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="relative flex-1">
              <input
                type="text"
                placeholder="Search products..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="w-full pl-4 pr-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white shadow-sm"
              />
            </div>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white shadow-sm"
            >
              <option value="all">All Categories</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          {/* Category chips */}
          {categories.length > 0 && (
            <div className="mt-4 flex flex-wrap gap-2">
              <button
                onClick={() => setSelectedCategory('all')}
                className={`px-3 py-1.5 rounded-full text-sm border transition-colors ${
                  selectedCategory === 'all'
                    ? 'bg-blue-600 text-white border-blue-600'
                    : 'bg-white text-gray-700 border-gray-200 hover:border-blue-300'
                }`}
              >
                All
              </button>
              {categories.map((category) => (
                <button
                  key={category}
                  onClick={() => setSelectedCategory(category)}
                  className={`px-3 py-1.5 rounded-full text-sm border transition-colors ${
                    selectedCategory === category
                      ? 'bg-blue-600 text-white border-blue-600'
                      : 'bg-white text-gray-700 border-gray-200 hover:border-blue-300'
                  }`}
                >
                  {category}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Products Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredProducts.map((product) => (
            <div
              key={product.id}
              className="group bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow overflow-hidden"
            >
              <div className="relative">
                <div className="aspect-square bg-white">
                  {product.imageUrl ? (
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      className="w-full h-full object-contain p-4"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-400 text-sm">
                      No Image
                    </div>
                  )}
                </div>
                <div className="absolute top-3 left-3">
                  <span className="text-xs px-2 py-1 rounded-full bg-blue-600 text-white shadow">
                    {product.category}
                  </span>
                </div>
              </div>
              <div className="p-4">
                <h3 className="text-lg font-semibold text-gray-900 group-hover:text-blue-700 transition-colors line-clamp-1">
                  {product.name}
                </h3>
                <p className="text-gray-600 text-sm mt-1 mb-3 line-clamp-2">{product.description}</p>
                <div className="flex items-center justify-between">
                  <p className="text-lg font-bold text-blue-700">${product.price.toFixed(2)}</p>
                  <button
                    onClick={() => handleProductDetail(product.id)}
                    className="inline-flex items-center px-3 py-2 text-sm font-medium rounded-md bg-blue-600 text-white hover:bg-blue-700"
                  >
                    View Details
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {filteredProducts.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">No products found</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Products;
