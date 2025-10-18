package com.comp5348.store.repository;

import com.comp5348.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search for products by name
    Optional<Product> findByName(String name);

    // Search all activated products
    List<Product> findByActiveTrue();

    // Search products by category
    List<Product> findByCategoryAndActiveTrue(String category);

    // Fuzzy search by name
    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1% AND p.active = true")
    List<Product> searchByName(String keyword);

    // Search all categories
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true")
    List<String> findAllCategories();
}