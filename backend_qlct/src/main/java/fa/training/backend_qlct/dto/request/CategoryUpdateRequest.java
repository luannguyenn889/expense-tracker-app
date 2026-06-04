package fa.training.backend_qlct.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {
    private String name;
    private String icon;
    private String type; // EXPENSE | INCOME
    private Long userId; // ID của người dùng sở hữu danh mục này (null nếu là danh mục hệ thống)
    private String color; // Màu sắc hiển thị (hex, ví dụ: #FF5733)
    private String description; // Mô tả ngắn về danh mục
    private Double monthlyBudget; // Ngân sách tháng (null nếu không giới hạn)
}
