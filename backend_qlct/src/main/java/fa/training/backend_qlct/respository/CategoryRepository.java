package fa.training.backend_qlct.respository;

import fa.training.backend_qlct.entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, String> {
    @Query("SELECT c FROM Categories c WHERE c.userId IS NULL OR c.userId = :userId")
    List<Categories> findByUserIdOrUserIdIsNull(@Param("userId") Long userId);

    @Query("SELECT c FROM Categories c WHERE (c.userId IS NULL OR c.userId = :userId) AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Categories> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("query") String query);
}
