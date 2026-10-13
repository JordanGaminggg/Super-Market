package com.supermarket.controller;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.supermarket.model.Product;
import com.supermarket.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles CSV bulk import of products.
 *
 * Expected CSV format (first row = header, ignored):
 *   name, category, price, gstPercent, stock, expiryDate (YYYY-MM-DD, optional)
 *
 * Example:
 *   name,category,price,gstPercent,stock,expiryDate
 *   Milk 500ml,Dairy,28.00,5,100,2025-06-30
 *   Sugar 1kg,Grocery,45.00,0,200,
 */
@RestController
@RequestMapping("/api/products")
public class CsvImportController {

    private final ProductRepository repo;

    public CsvImportController(ProductRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "CSV file is empty."));
        }

        List<String> errors  = new ArrayList<>();
        List<Product> saved  = new ArrayList<>();
        int rowNum = 1; // starts after header

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "CSV has no data rows."));
            }

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                rowNum = i;
                String[] cols = rows.get(i);
                if (cols.length < 3) { errors.add("Row " + rowNum + ": too few columns (need at least name, category, price)."); continue; }

                String name = cols[0].trim();
                if (name.isEmpty()) { errors.add("Row " + rowNum + ": product name is blank."); continue; }

                try {
                    Product p = new Product();
                    p.setName(name);
                    p.setCategory(cols.length > 1 ? cols[1].trim() : "");
                    p.setPrice(new BigDecimal(cols[2].trim()));
                    p.setGstPercent(cols.length > 3 && !cols[3].isBlank() ? new BigDecimal(cols[3].trim()) : BigDecimal.ZERO);
                    p.setStock(cols.length > 4 && !cols[4].isBlank() ? Integer.parseInt(cols[4].trim()) : 0);
                    if (cols.length > 5 && !cols[5].isBlank()) {
                        p.setExpiryDate(LocalDate.parse(cols[5].trim()));
                    }
                    saved.add(repo.save(p));
                } catch (Exception ex) {
                    errors.add("Row " + rowNum + ": " + ex.getMessage());
                }
            }
        } catch (Exception | CsvException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to parse CSV: " + e.getMessage()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("imported", saved.size());
        result.put("errors",   errors);
        result.put("message",  saved.size() + " product(s) imported" + (errors.isEmpty() ? "." : ", " + errors.size() + " row(s) skipped."));
        return ResponseEntity.ok(result);
    }

    /** Returns a sample CSV template for download */
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> template() {
        String csv = "name,category,price,gstPercent,stock,expiryDate\n" +
                     "Milk 500ml,Dairy,28.00,5,100,2025-12-31\n" +
                     "Sugar 1kg,Grocery,45.00,0,200,\n" +
                     "Biscuits,Snacks,20.00,12,150,2025-09-01\n";
        byte[] bytes = csv.getBytes();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"product_import_template.csv\"")
                .header("Content-Type", "text/csv")
                .body(bytes);
    }
}
