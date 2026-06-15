package fa.training.backend_qlct.repository;

import fa.training.backend_qlct.entities.SavingGoals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoals, Long> {
    java.util.List<SavingGoals> findByUserId(Long userId);
}