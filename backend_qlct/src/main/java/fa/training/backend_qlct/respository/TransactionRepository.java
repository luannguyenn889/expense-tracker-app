package fa.training.backend_qlct.respository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import fa.training.backend_qlct.entities.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.wallet w " +
           "WHERE t.userId = :userId " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:walletId IS NULL OR t.walletId = :walletId) " + 
           "AND (:query IS NULL OR :query = '' OR " +
           "     LOWER(t.note) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     t.categoryId IN :categoryIds) " +
           "ORDER BY t.transactionDate DESC, t.id DESC")
    Page<Transaction> searchTransactions(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("type") String type,  
                                         @Param("walletId") Long walletId,
                                         @Param("query") String query,
                                         @Param("categoryIds") List<String> categoryIds,
                                         Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t " +
           "WHERE (t.walletId = :walletId OR t.toWalletId = :walletId) AND t.userId = :userId")
    boolean existsByWalletIdOrToWalletId(@Param("walletId") Long walletId, @Param("userId") Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findTop10ByUserIdOrderByTransactionDateDescIdDesc(Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.categoryId = :categoryId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate <= :endDate")
    java.math.BigDecimal sumExpenseByCategoryIdAndDate(
            @Param("userId") Long userId,
            @Param("categoryId") String categoryId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount WHEN t.type = 'INCOME' THEN -t.amount ELSE 0 END) " +
            "FROM Transaction t WHERE t.userId = :userId AND t.categoryId = :categoryId " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal calculateNetSpendingByCategoryIdAndDate(@Param("userId") Long userId,
                                                       @Param("categoryId") String categoryId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}