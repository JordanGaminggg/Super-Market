package com.supermarket.dto;

import java.math.BigDecimal;
import java.util.List;

// This is the JSON the billing page sends to the server
public record BillRequest(
        String customerName,
        String customerPhone,
        String paymentMode,
        BigDecimal discount,
        List<Item> items) {

    public record Item(Long productId, int quantity) { }
}
