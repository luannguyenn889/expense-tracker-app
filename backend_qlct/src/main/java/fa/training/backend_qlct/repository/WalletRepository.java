package fa.training.backend_qlct.repository;

import fa.training.backend_qlct.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :walletId")
    void updateBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.status = 'ACTIVE'")
    List<Wallet> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    List<Wallet> findAllByUserId(@Param("userId") Long userId);
}