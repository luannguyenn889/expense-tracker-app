package fa.training.backend_qlct.dto.request;

import jakarta.persistence.Id;

public class CategoryCreationRequest {


    private String name;
    private String icon;
    private String type;



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
}
