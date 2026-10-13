package com.supermarket.controller;

import com.supermarket.model.Product;
import com.supermarket.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    /** List all products with optional search + category filter */
    @GetMapping
    public List<Product> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        boolean hasSearch   = search   != null && !search.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasSearch && hasCategory)
            return repo.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(search, category);
        if (hasSearch)
            return repo.findByNameContainingIgnoreCase(search);
        if (hasCategory)
            return repo.findByCategoryIgnoreCase(category);
        return repo.findAll();
    }

    /** Returns sorted distinct category names for the filter dropdown */
    @GetMapping("/categories")
    public List<String> categories() {
        return repo.findDistinctCategories();
    }

    @PostMapping
    public Product add(@RequestBody Product p) {
        check(p);
        p.setId(null);
        return repo.save(p);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product p) {
        check(p);
        repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        p.setId(id);
        return repo.save(p);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    /** Restock: add qty units to existing stock */
    @PatchMapping("/{id}/restock")
    public Product restock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int qty = body.getOrDefault("qty", 0);
        if (qty <= 0) throw new IllegalArgumentException("Restock quantity must be positive.");
        Product p = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        p.setStock(p.getStock() + qty);
        return repo.save(p);
    }

    private void check(Product p) {
        if (p.getName() == null || p.getName().isBlank())
            throw new IllegalArgumentException("Product name is required.");
        if (p.getPrice() == null || p.getPrice().signum() < 0)
            throw new IllegalArgumentException("Enter a valid price.");
        if (p.getStock() < 0)
            throw new IllegalArgumentException("Stock cannot be negative.");
        if (p.getGstPercent() == null) p.setGstPercent(BigDecimal.ZERO);
    }
}
