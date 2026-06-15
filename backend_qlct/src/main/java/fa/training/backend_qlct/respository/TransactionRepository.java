package fa.training.backend_qlct.respository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fa.training.backend_qlct.entities.Transaction;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // ========================= Dashboard =========================

    @Query(value = "SELECT " +
            "DATE_FORMAT(t.transaction_date, '%Y-%m') AS `yearMonth`, " +
            "CAST(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS DOUBLE) AS `income`, " +
            "CAST(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS DOUBLE) AS `expense` " +
            "FROM transactions t " +
            "WHERE t.user_id = :userId " +
            "AND t.transaction_date >= DATE_SUB(CURDATE(), INTERVAL :months MONTH) " +
            "GROUP BY DATE_FORMAT(t.transaction_date, '%Y-%m') " +
            "ORDER BY `yearMonth` ASC", nativeQuery = true)
    List<Map<String, Object>> getMonthlyChartData(@Param("userId") Long userId,
                                                  @Param("months") int months);

    @Query(value = "SELECT " +
            "CAST(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS DOUBLE) AS `totalIncome`, " +
            "CAST(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS DOUBLE) AS `totalExpense` " +
            "FROM transactions t " +
            "WHERE t.user_id = :userId " +
            "AND t.transaction_date >= DATE_SUB(CURDATE(), INTERVAL :months MONTH)", nativeQuery = true)
    Map<String, Object> getDashboardSummary(@Param("userId") Long userId,
                                            @Param("months") int months);

    @Query(value = "SELECT w.name AS `name`, CAST(w.balance AS DOUBLE) AS `balance` " +
            "FROM wallets w WHERE w.user_id = :userId", nativeQuery = true)
    List<Map<String, Object>> getWallets(@Param("userId") Long userId);

    @Query(value = "SELECT t.note AS `title`, CAST(t.amount AS DOUBLE) AS `amount`, t.type AS `type` " +
            "FROM transactions t WHERE t.user_id = :userId " +
            "ORDER BY t.transaction_date DESC LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getRecentTransactions(@Param("userId") Long userId);

    @Query(value = "SELECT " +
            "c.name AS `name`, " +
            "CAST(SUM(t.amount) AS DOUBLE) AS `amount`, " +
            "c.color AS `color`, " +
            "CAST(COALESCE((SUM(t.amount) * 100 / " +
            "(SELECT NULLIF(SUM(amount),0) FROM transactions WHERE user_id=:userId AND type='EXPENSE' AND MONTH(transaction_date)=:month AND YEAR(transaction_date)=:year)),0) AS DOUBLE) AS `percentage`, " +
            "CAST(IFNULL(MAX(b.amount),0) AS DOUBLE) AS `budgetAmount` " +
            "FROM transactions t " +
            "JOIN categories c ON t.category_id = c.id " +
            "LEFT JOIN budgets b ON b.category_id = c.id " +
            "AND b.user_id = :userId " +
            "AND b.month = :month " +
            "AND b.year = :year " +
            "WHERE t.user_id = :userId " +
            "AND t.type='EXPENSE' " +
            "AND MONTH(t.transaction_date)=:month " +
            "AND YEAR(t.transaction_date)=:year " +
            "GROUP BY c.id,c.name,c.color", nativeQuery = true)
    List<Map<String, Object>> getExpenseByCategory(@Param("userId") Long userId,
                                                   @Param("month") int month,
                                                   @Param("year") int year);

    @Query("""
        SELECT COALESCE(SUM(t.amount),0)
        FROM Transaction t
        WHERE t.userId = :userId
          AND t.type='EXPENSE'
          AND MONTH(t.transactionDate)=:month
          AND YEAR(t.transactionDate)=:year
    """)
    BigDecimal sumExpenseByMonth(@Param("userId") Long userId,
                                 @Param("month") int month,
                                 @Param("year") int year);

    // ========================= Search =========================

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.wallet w " +
            "WHERE t.userId = :userId " +
            "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:walletId IS NULL OR t.walletId = :walletId) " +
            "AND (:query IS NULL OR :query = '' OR " +
            "LOWER(t.note) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "t.categoryId IN :categoryIds) " +
            "ORDER BY t.transactionDate DESC, t.id DESC")
    Page<Transaction> searchTransactions(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") String type,
            @Param("walletId") Long walletId,
            @Param("query") String query,
            @Param("categoryIds") List<String> categoryIds,
            Pageable pageable);

    // ========================= Reports =========================

    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate);

    List<Transaction> findTop10ByUserIdOrderByTransactionDateDescIdDesc(Long userId);

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t " +
            "WHERE t.userId=:userId " +
            "AND t.categoryId=:categoryId " +
            "AND t.type='EXPENSE' " +
            "AND t.transactionDate>=:startDate " +
            "AND t.transactionDate<=:endDate")
    BigDecimal sumExpenseByCategoryIdAndDate(
            @Param("userId") Long userId,
            @Param("categoryId") String categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(CASE WHEN t.type='EXPENSE' THEN t.amount WHEN t.type='INCOME' THEN -t.amount ELSE 0 END) " +
            "FROM Transaction t " +
            "WHERE t.userId=:userId " +
            "AND t.categoryId=:categoryId " +
            "AND t.transactionDate>=:startDate " +
            "AND t.transactionDate<=:endDate")
    BigDecimal calculateNetSpendingByCategoryIdAndDate(
            @Param("userId") Long userId,
            @Param("categoryId") String categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ========================= Wallet =========================

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t " +
            "WHERE (t.walletId = :walletId OR t.toWalletId = :walletId) " +
            "AND t.userId = :userId")
    boolean existsByWalletIdOrToWalletId(
            @Param("walletId") Long walletId,
            @Param("userId") Long userId);
}