package com.supermarket.service;

import com.supermarket.dto.BillRequest;
import com.supermarket.model.Bill;
import com.supermarket.model.BillItem;
import com.supermarket.model.Product;
import com.supermarket.repository.BillRepository;
import com.supermarket.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BillService {

    private final BillRepository billRepo;
    private final ProductRepository productRepo;

    public BillService(BillRepository billRepo, ProductRepository productRepo) {
        this.billRepo = billRepo;
        this.productRepo = productRepo;
    }

    /** Creates a bill, calculates GST + totals and reduces stock - all in ONE transaction. */
    @Transactional
    public Bill createBill(BillRequest req) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Add at least one product.");
        }

        Bill bill = new Bill();
        bill.setCustomerName(blank(req.customerName()) ? "Walk-in Customer" : req.customerName().trim());
        bill.setCustomerPhone(req.customerPhone());
        bill.setPaymentMode(blank(req.paymentMode()) ? "Cash" : req.paymentMode());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;

        for (BillRequest.Item cartItem : req.items()) {
            if (cartItem.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be at least 1.");
            }
            Product p = productRepo.findById(cartItem.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + cartItem.productId()));

            if (p.getStock() < cartItem.quantity()) {
                throw new IllegalArgumentException("Not enough stock for " + p.getName()
                        + " (available: " + p.getStock() + ")");
            }
            p.setStock(p.getStock() - cartItem.quantity());     // reduce stock

            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(cartItem.quantity()));
            BigDecimal gst = line.multiply(p.getGstPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProductId(p.getId());
            item.setProductName(p.getName());
            item.setPrice(p.getPrice());
            item.setGstPercent(p.getGstPercent());
            item.setQuantity(cartItem.quantity());
            item.setLineTotal(line);
            bill.getItems().add(item);

            subtotal = subtotal.add(line);
            gstTotal = gstTotal.add(gst);
        }

        BigDecimal discount = req.discount() == null ? BigDecimal.ZERO : req.discount();
        BigDecimal grandTotal = subtotal.add(gstTotal).subtract(discount);
        if (discount.signum() < 0 || grandTotal.signum() < 0) {
            throw new IllegalArgumentException("Invalid discount amount.");
        }

        bill.setSubtotal(subtotal);
        bill.setGstAmount(gstTotal);
        bill.setDiscount(discount);
        bill.setGrandTotal(grandTotal);
        return billRepo.save(bill);
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
