package com.supermarket.controller;

import com.supermarket.dto.BillRequest;
import com.supermarket.model.Bill;
import com.supermarket.model.BillItem;
import com.supermarket.repository.BillRepository;
import com.supermarket.service.BillService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService service;
    private final BillRepository repo;

    public BillController(BillService service, BillRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping
    public Bill create(@RequestBody BillRequest request) {
        return service.createBill(request);
    }

    @GetMapping
    public List<Bill> all() {
        return repo.findAllByOrderByBillDateDesc();
    }

    @GetMapping("/{id}")
    public Bill one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Bill not found"));
    }

    /** Filter bills: ?from=2024-01-01&to=2024-12-31&customer=John&mode=Cash */
    @GetMapping("/filter")
    public List<Bill> filter(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String mode) {

        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to   != null) ? to.atTime(23, 59, 59) : null;
        return repo.filter(fromDt, toDt, customer, mode);
    }

    /** Export filtered bills as CSV */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String mode) {

        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to   != null) ? to.atTime(23, 59, 59) : null;
        List<Bill> bills = repo.filter(fromDt, toDt, customer, mode);

        StringBuilder csv = new StringBuilder();
        csv.append("Bill No,Date,Customer,Phone,Payment,Subtotal,GST,Discount,Grand Total\n");
        for (Bill b : bills) {
            csv.append(String.format("#%d,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f\n",
                    b.getId(),
                    b.getBillDate().toString().replace("T", " "),
                    safe(b.getCustomerName()), safe(b.getCustomerPhone()),
                    safe(b.getPaymentMode()),
                    b.getSubtotal(), b.getGstAmount(), b.getDiscount(), b.getGrandTotal()));
        }

        byte[] bytes = csv.toString().getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bills_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private String safe(String s) {
        return s == null ? "" : "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
