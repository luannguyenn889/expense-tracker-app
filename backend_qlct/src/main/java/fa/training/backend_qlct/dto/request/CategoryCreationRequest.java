package fa.training.backend_qlct.dto.request;

public class CategoryCreationRequest {

    private String name;
    private String icon;
    private String type;
    private Long userId; // ID của người dùng sở hữu danh mục này (null nếu là danh mục hệ thống)

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
