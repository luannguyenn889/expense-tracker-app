package fa.training.backend_qlct.respository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fa.training.backend_qlct.entities.Categories;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, String> {

    // Lấy tất cả danh mục của người dùng và danh mục mặc định
    @Query("SELECT c FROM Categories c WHERE c.userId IS NULL OR c.userId = :userId")
    List<Categories> findByUserIdOrUserIdIsNull(@Param("userId") Long userId);

    // Tìm danh mục theo tên
    @Query("SELECT c FROM Categories c " +
           "WHERE (c.userId IS NULL OR c.userId = :userId) " +
           "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Categories> findByUserIdAndNameContaining(@Param("userId") Long userId,
                                                   @Param("query") String query);

    // Kiểm tra trùng tên danh mục
    boolean existsByNameAndUserId(String name, Long userId);

    // Tìm theo loại (INCOME/EXPENSE)
    List<Categories> findByType(String type);

    // Tìm theo user và loại
    List<Categories> findByUserIdAndType(Long userId, String type);
    
    Optional<Categories> findById(String id);
    List<Categories> findByUserId(Long userId);
}