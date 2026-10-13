package com.supermarket.controller;

import com.supermarket.repository.BillRepository;
import com.supermarket.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class DashboardController {

    private final ProductRepository products;
    private final BillRepository bills;

    public DashboardController(ProductRepository products, BillRepository bills) {
        this.products = products;
        this.bills = bills;
    }

    @GetMapping("/api/dashboard")
    public Map<String, Object> stats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalProducts", products.count());
        m.put("lowStock", products.findByStockLessThan(10));
        m.put("todaySales", bills.totalSalesSince(startOfToday));
        m.put("todayBills", bills.countByBillDateGreaterThanEqual(startOfToday));
        m.put("totalBills", bills.count());
        return m;
    }

    /** Returns daily sales for the last 30 days for the chart */
    @GetMapping("/api/dashboard/chart")
    public Map<String, Object> chart() {
        LocalDateTime since = LocalDate.now().minusDays(29).atStartOfDay();
        List<Object[]> rows = bills.dailySalesSince(since);

        List<String> labels = new ArrayList<>();
        List<Double> data   = new ArrayList<>();
        for (Object[] row : rows) {
            labels.add(row[0].toString());
            data.add(((Number) row[1]).doubleValue());
        }
        return Map.of("labels", labels, "data", data);
    }

    /** Top 5 products by revenue in last 30 days */
    @GetMapping("/api/dashboard/top-products")
    public Map<String, Object> topProducts() {
        LocalDateTime since = LocalDate.now().minusDays(29).atStartOfDay();
        List<Object[]> rows = bills.topProducts(since);

        List<String> labels = new ArrayList<>();
        List<Double> data   = new ArrayList<>();
        for (Object[] row : rows) {
            labels.add(row[0].toString());
            data.add(((Number) row[1]).doubleValue());
        }
        return Map.of("labels", labels, "data", data);
    }
}
