package fa.training.backend_qlct.repository;

import fa.training.backend_qlct.entities.Budgets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budgets, Long> {
    // Hàm này giúp kiểm tra xem đã có ngân sách cho category đó trong tháng/năm này chưa
    boolean existsByCategoryIdAndUserIdAndMonthAndYear(String categoryId, Long userId, Integer month, Integer year);
    // Thêm hàm này vào BudgetRepository.java
    java.util.List<Budgets> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
}