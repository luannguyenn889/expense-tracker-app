package fa.training.backend_qlct.respository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fa.training.backend_qlct.entities.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> ,JpaSpecificationExecutor<Transaction>{

    // 1. Lấy dữ liệu biểu đồ cột thu nhập và chi tiêu gom cụm theo THÁNG (Hiển thị biểu đồ cột)
    @Query(value = "SELECT " +
                   "  DATE_FORMAT(t.transaction_date, '%Y-%m') AS `yearMonth`, " +
                   "  CAST(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS DOUBLE) AS `income`, " +
                   "  CAST(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS DOUBLE) AS `expense` " +
                   "FROM transactions t " +
                   "WHERE t.user_id = :userId " +
                   "  AND t.transaction_date >= DATE_SUB(CURDATE(), INTERVAL :months MONTH) " +
                   "GROUP BY DATE_FORMAT(t.transaction_date, '%Y-%m') " +
                   "ORDER BY `yearMonth` ASC", nativeQuery = true)
    List<Map<String, Object>> getMonthlyChartData(@Param("userId") Long userId, @Param("months") int months);

    // 3. Tính tổng thu và tổng chi phục vụ cho các thẻ thống kê Summary Cards (Tổng thu nhập, Tổng chi tiêu)
    @Query(value = "SELECT " +
                   "  CAST(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS DOUBLE) AS `totalIncome`, " +
                   "  CAST(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS DOUBLE) AS `totalExpense` " +
                   "FROM transactions t " +
                   "WHERE t.user_id = :userId " +
                   "  AND t.transaction_date >= DATE_SUB(CURDATE(), INTERVAL :months MONTH)", nativeQuery = true)
    Map<String, Object> getDashboardSummary(@Param("userId") Long userId, @Param("months") int months);

    // 4. Lấy danh sách ví tài sản của người dùng
    @Query(value = "SELECT w.name AS `name`, CAST(w.balance AS DOUBLE) AS `balance` " +
                   "FROM wallets w WHERE w.user_id = :userId", nativeQuery = true)
    List<Map<String, Object>> getWallets(@Param("userId") Long userId);

    // 5. Lấy danh sách 5 giao dịch phát sinh gần đây nhất
    @Query(value = "SELECT t.note AS `title`, CAST(t.amount AS DOUBLE) AS `amount`, t.type AS `type` " +
                   "FROM transactions t WHERE t.user_id = :userId " +
                   "ORDER BY t.transaction_date DESC LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getRecentTransactions(@Param("userId") Long userId);

    // 6. Phân tích chi tiêu chi tiết theo danh mục, tỉ lệ phần trăm và hạn mức ngân sách (Budget)
    // Đã bọc COALESCE và NULLIF để phòng chống lỗi chia cho 0 khi tháng được chọn chưa có dữ liệu chi tiêu
    @Query(value = "SELECT " +
                   "  c.name AS `name`, " +
                   "  CAST(SUM(t.amount) AS DOUBLE) AS `amount`, " +
                   "  c.color AS `color`, " +
                   "  CAST(COALESCE((SUM(t.amount) * 100 / " +
                   "    (SELECT NULLIF(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'EXPENSE' AND MONTH(transaction_date) = :month AND YEAR(transaction_date) = :year)" +
                   "  ), 0) AS DOUBLE) AS `percentage`, " +
                   "  CAST(IFNULL(MAX(b.amount), 0) AS DOUBLE) AS `budgetAmount` " +
                   "FROM transactions t " +
                   "JOIN categories c ON t.category_id = c.id " +
                   "LEFT JOIN budgets b ON b.category_id = c.id " +
                   "  AND b.user_id = :userId " +
                   "  AND b.month = :month " +
                   "  AND b.year = :year " +
                   "WHERE t.user_id = :userId AND t.type = 'EXPENSE' " +
                   "  AND MONTH(t.transaction_date) = :month " +
                   "  AND YEAR(t.transaction_date) = :year " +
                   "GROUP BY c.id, c.name, c.color", nativeQuery = true)
    List<Map<String, Object>> getExpenseByCategory(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("""
    SELECT COALESCE(SUM(t.amount),0)
    FROM Transaction t
    WHERE t.userId = :userId
    AND t.type = fa.training.backend_qlct.entities.Transaction.TransactionType.EXPENSE
    AND MONTH(t.transactionDate) = :month
    AND YEAR(t.transactionDate) = :year
    """)
    BigDecimal sumExpenseByMonth(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year);
}
