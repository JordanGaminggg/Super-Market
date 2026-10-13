package com.supermarket.repository;

import com.supermarket.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findAllByOrderByBillDateDesc();

    @Query("select coalesce(sum(b.grandTotal), 0) from Bill b where b.billDate >= :start")
    BigDecimal totalSalesSince(@Param("start") LocalDateTime start);

    long countByBillDateGreaterThanEqual(LocalDateTime start);

    /** Filter bills by optional date range, customer name, or payment mode */
    @Query("""
        SELECT b FROM Bill b
        WHERE (:from IS NULL OR b.billDate >= :from)
          AND (:to   IS NULL OR b.billDate <= :to)
          AND (:customer IS NULL OR :customer = '' OR LOWER(b.customerName) LIKE LOWER(CONCAT('%', :customer, '%')))
          AND (:mode IS NULL OR :mode = '' OR b.paymentMode = :mode)
        ORDER BY b.billDate DESC
    """)
    List<Bill> filter(
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            @Param("customer") String customer,
            @Param("mode")     String mode);

    /** Last 7 days: day label + total sales per day */
    @Query(value = """
        SELECT DATE(b.bill_date) as day, COALESCE(SUM(b.grand_total), 0) as total
        FROM bills b
        WHERE b.bill_date >= :since
        GROUP BY DATE(b.bill_date)
        ORDER BY day
    """, nativeQuery = true)
    List<Object[]> dailySalesSince(@Param("since") LocalDateTime since);

    /** Top 5 best-selling products by revenue */
    @Query(value = """
        SELECT bi.product_name, COALESCE(SUM(bi.line_total), 0) as revenue
        FROM bill_items bi
        JOIN bills b ON bi.bill_id = b.id
        WHERE b.bill_date >= :since
        GROUP BY bi.product_name
        ORDER BY revenue DESC
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> topProducts(@Param("since") LocalDateTime since);
}
