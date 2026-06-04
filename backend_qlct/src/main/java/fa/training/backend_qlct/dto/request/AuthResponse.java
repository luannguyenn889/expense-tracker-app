package fa.training.backend_qlct.dto.request;

import fa.training.backend_qlct.entities.Users;

public class AuthResponse {
    private String accessToken;
    private Users user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
