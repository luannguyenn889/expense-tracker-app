package fa.training.backend_qlct.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import fa.training.backend_qlct.dto.request.AuthResponse;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    
    // Tạm thời comment JwtService vì bạn chưa tạo class này
    // @Autowired
    // private JwtService jwtService;

    // Thay thế chuỗi này bằng Client ID thật của bạn từ Google Cloud Console
    public static final String GOOGLE_CLIENT_ID = "569426721711-6ub3hgomi74b201otif6cpe3oq53s5fj.apps.googleusercontent.com";

    public AuthResponse processGoogleLogin(String tokenFromAngular) throws Exception {
        
        // 1. Xác thực token với Google
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();
                
        GoogleIdToken idToken = verifier.verify(tokenFromAngular);
        
        if (idToken == null) {
            throw new Exception("Invalid ID token");
        }
        
        // Lấy thông tin user từ Google Token
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        
        // Lấy chuỗi ký tự trước dấu @ làm username
        String extractedUsername = email != null ? email.split("@")[0] : "GoogleUser";
        
        // 2. Kiểm tra nếu user đã tồn tại trong database, nếu chưa thì tạo mới
        Optional<Users> userOpt = userRepository.findByUsername(extractedUsername);
        Users user;
        
        if (userOpt.isEmpty()) {
            user = new Users();
            user.setUsername(extractedUsername); // Dùng username vừa tách được
            
            // Tách tên và họ (cơ bản) từ fullname của Google
            if (name != null && name.contains(" ")) {
                user.setFirstname(name.substring(name.lastIndexOf(" ") + 1));
                user.setLastname(name.substring(0, name.lastIndexOf(" ")));
            } else {
                user.setFirstname(name);
            }
            
            // Set một password ngẫu nhiên hoặc rỗng vì login bằng Google không cần password
            user.setPassword(""); 
            
            userRepository.save(user);
        } else {
            user = userOpt.get();
        }

        // 3. Tạo JWT token cho user (Chưa có JwtService nên mình mock 1 chuỗi)
        // String jwtToken = jwtService.generateToken(user);
        String jwtToken = "GENERATED_JWT_TOKEN_FOR_USER_" + user.getUsername();
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(jwtToken);
        response.setUser(user);
        return response;
    }
}
