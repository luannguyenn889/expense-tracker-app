package fa.training.backend_qlct.dto.request;

public class CategoryCreationRequest {

    private String name;
    private String icon;
    private String type; // EXPENSE | INCOME
    private Long userId; // ID của người dùng sở hữu danh mục này (null nếu là danh mục hệ thống)
    private String color; // Màu sắc hiển thị (hex, ví dụ: #FF5733)
    private String description; // Mô tả ngắn về danh mục
    private Double monthlyBudget; // Ngân sách tháng (null nếu không giới hạn)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }
}
