package com.comp5348.store;

import com.comp5348.store.dto.ProductDTO;
import com.comp5348.store.entity.Product;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductService Unit testing
 * Test all functionalities of the product service
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        // Prepare test data
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setCategory("Electronics");
        testProduct.setActive(true);

        testProductDTO = new ProductDTO();
        testProductDTO.setId(1L);
        testProductDTO.setName("Test Product");
        testProductDTO.setDescription("Test Description");
        testProductDTO.setPrice(new BigDecimal("99.99"));
        testProductDTO.setCategory("Electronics");
        testProductDTO.setActive(true);
    }

    @Test
    void testCreateProduct_Success() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDTO result = productService.createProduct(testProductDTO);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(new BigDecimal("99.99"), result.getPrice());
        assertEquals("Electronics", result.getCategory());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetProductById_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ProductDTO result = productService.getProductById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(999L));
    }

    @Test
    void testGetAllActiveProducts_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<ProductDTO> results = productService.getAllActiveProducts();

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Product", results.get(0).getName());
        verify(productRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testGetProductsByCategory_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategoryAndActiveTrue("Electronics")).thenReturn(products);

        // When
        List<ProductDTO> results = productService.getProductsByCategory("Electronics");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Electronics", results.get(0).getCategory());
        verify(productRepository, times(1)).findByCategoryAndActiveTrue("Electronics");
    }

    @Test
    void testUpdateProduct_Success() {
        // Given
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(new BigDecimal("149.99"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(999L, testProductDTO));
    }

    @Test
    void testDeleteProduct_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        assertFalse(testProduct.getActive()); // 验证软删除
    }

    @Test
    void testDeleteProduct_NotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(999L));
    }

    @Test
    void testSearchProducts_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.searchByName("Test")).thenReturn(products);

        // When
        List<ProductDTO> results = productService.searchProducts("Test");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Product", results.get(0).getName());
        verify(productRepository, times(1)).searchByName("Test");
    }

    @Test
    void testSearchProducts_NoResults() {
        // Given
        when(productRepository.searchByName("NonExistent")).thenReturn(Arrays.asList());

        // When
        List<ProductDTO> results = productService.searchProducts("NonExistent");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetAllCategories_Success() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Books", "Clothing");
        when(productRepository.findAllCategories()).thenReturn(categories);

        // When
        List<String> results = productService.getAllCategories();

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.contains("Electronics"));
        verify(productRepository, times(1)).findAllCategories();
    }

    @Test
    void testProductExists_True() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        boolean exists = productService.productExists(1L);

        // Then
        assertTrue(exists);
        verify(productRepository, times(1)).existsById(1L);
    }

    @Test
    void testProductExists_False() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);

        // When
        boolean exists = productService.productExists(999L);

        // Then
        assertFalse(exists);
        verify(productRepository, times(1)).existsById(999L);
    }
}