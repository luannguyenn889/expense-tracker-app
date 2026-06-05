package fa.training.backend_qlct.respository;

import fa.training.backend_qlct.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.wallet w " +
           "WHERE t.userId = :userId " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:walletId IS NULL OR t.walletId = :walletId) " + 
           "ORDER BY t.transactionDate DESC, t.id DESC")
    Page<Transaction> searchTransactions(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("type") String type,  
                                         @Param("walletId") Long walletId,
                                         Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t " +
           "WHERE (t.walletId = :walletId OR t.toWalletId = :walletId) AND t.userId = :userId")
    boolean existsByWalletIdOrToWalletId(@Param("walletId") Long walletId, @Param("userId") Long userId);
}