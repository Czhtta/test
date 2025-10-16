package com.comp5348.store.service;

import com.comp5348.store.dto.ProductDTO;
import com.comp5348.store.entity.Product;

import java.util.List;

public interface ProductService {

    // Create product
    ProductDTO createProduct(ProductDTO productDTO);

    // Update product
    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    // Delete product
    void deleteProduct(Long id);

    // Search product by id
    ProductDTO getProductById(Long id);

    // Search all active products
    List<ProductDTO> getAllActiveProducts();

    // Search products by category
    List<ProductDTO> getProductsByCategory(String category);

    // Search products
    List<ProductDTO> searchProducts(String keyword);

    // Get all categories
    List<String> getAllCategories();

    // Check whether the goods exist
    boolean productExists(Long id);
}
