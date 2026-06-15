package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.*;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.respository.UserRepository;
import fa.training.backend_qlct.service.AuthService;
import fa.training.backend_qlct.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
// Đổi hẳn sang cổng Angular cụ thể để tránh lỗi CORS tiềm ẩn khi xử lý Session/Cookie
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true") 
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // --- NORMAL LOGIN (DUNG HỢP & FIX LỖI 500) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // In log kiểm tra để chắc chắn dữ liệu truyền xuống thành công
        System.out.println("Dữ liệu nhận từ Angular: Username = " + request.getUsername() + ", Password = " + request.getPassword());
        
        try {
            // Tìm user theo username trong database thông qua Repository công thức chuẩn
            Optional<Users> userOptional = userRepository.findByUsername(request.getUsername());

            if (!userOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Tên đăng nhập không tồn tại.");
            }

            Users user = userOptional.get();

            // FIX LỖI 500: Kiểm tra mật khẩu an toàn tuyệt đối, loại bỏ nguy cơ NullPointerException do trống dữ liệu
            String dbPassword = user.getPassword() != null ? user.getPassword().trim() : "";
            String inputPassword = request.getPassword() != null ? request.getPassword().trim() : "";

            if (!dbPassword.equals(inputPassword)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Mật khẩu không đúng.");
            }

            // Xóa password trước khi trả về Client để tăng tính bảo mật thông tin
            user.setPassword(null); 

            // Đóng gói cấu trúc Response khớp hoàn toàn với Client Angular yêu cầu
            AuthResponse response = new AuthResponse();
            response.setAccessToken("normal_login_token");
            response.setUser(user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Lỗi phát sinh hệ thống tại hàm Login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // --- GOOGLE LOGIN (DUNG HỢP LUỒNG XỬ LÝ) ---
    @PostMapping("/google-login")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            // Cách xử lý 1: Nếu bạn muốn chạy bằng idToken thực tế qua AuthService
            // String googleToken = request.getIdToken(); 
            // AuthResponse response = authService.processGoogleLogin(googleToken);
            // return ResponseEntity.ok(response);

            // Cách xử lý 2: Giả lập đăng nhập bằng tài khoản mẫu 'admin' theo luồng cũ của bạn
            String demoUsername = "admin"; 
            Optional<Users> userOptional = userRepository.findByUsername(demoUsername);

            if (!userOptional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Không tìm thấy người dùng liên kết với tài khoản này.");
            }

            Users user = userOptional.get();
            user.setPassword(null); 

            AuthResponse response = new AuthResponse();
            response.setAccessToken("google_login_token_" + System.currentTimeMillis());
            response.setUser(user);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google login failed: " + e.getMessage());
        }
    }

    // --- NORMAL REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreationRequest request) {
        try {
            Users newUser = userService.createRequest(request);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // --- UPDATE USERS ---
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateRequest request) {
        try {
            Users newUser = userService.updateRequest(userId, request);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}