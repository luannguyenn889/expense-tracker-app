package fa.training.backend_qlct.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {
    private String name;
    private String icon;
    private String type;
    private Long userId; // ID của người dùng sở hữu danh mục này (null nếu là danh mục hệ thống)

}
