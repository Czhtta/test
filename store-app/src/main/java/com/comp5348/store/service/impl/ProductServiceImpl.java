package com.comp5348.store.service.impl;

import com.comp5348.store.dto.ProductDTO;
import com.comp5348.store.entity.Product;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        Product product = toEntity(productDTO);
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return toDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Update field
        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }
        if (productDTO.getCategory() != null) {
            product.setCategory(productDTO.getCategory());
        }
        if (productDTO.getImageUrl() != null) {
            product.setImageUrl(productDTO.getImageUrl());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", id);

        return toDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(false);
        productRepository.save(product);

        log.info("Product soft deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return toDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        log.info("Fetching all active products");

        return productRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);

        return productRepository.findByCategoryAndActiveTrue(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);

        return productRepository.searchByName(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.info("Fetching all product categories");
        return productRepository.findAllCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean productExists(Long id) {
        return productRepository.existsById(id);
    }



    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.getActive());
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        return product;
    }
}