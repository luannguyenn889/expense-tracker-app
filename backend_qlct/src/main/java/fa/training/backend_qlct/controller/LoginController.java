// // Deleted because logic is moved to AuthController
// package fa.training.backend_qlct.controller;

// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import fa.training.backend_qlct.dto.request.AuthResponse;
// import fa.training.backend_qlct.dto.request.LoginRequest;
// import fa.training.backend_qlct.entities.Users;
// import fa.training.backend_qlct.respository.UserRepository;

// /**
//  * REST API đăng nhập cho Angular
//  * URL gọi từ Angular:
//  * POST http://localhost:8080/api/auth/login
//  */
// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "http://localhost:4200")
// public class LoginController {

//     @Autowired
//     private UserRepository userRepository;

//     /**
//      * API đăng nhập bằng username/password
//      */
//     @PostMapping("/login")
//     public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//         System.out.println("Dữ liệu nhận từ Angular: Username = " + request.getUsername() + ", Password = " + request.getPassword());
//         // Tìm user theo username trong database
//         Optional<Users> userOptional = userRepository.findByUsername(request.getUsername());

//         // KHẮC PHỤC: Kiểm tra đối tượng Optional xem có tồn tại tài khoản không
//         if (!userOptional.isPresent()) {
//             return ResponseEntity
//                     .status(HttpStatus.UNAUTHORIZED)
//                     .body("Tên đăng nhập không tồn tại.");
//         }

//         Users user = userOptional.get();

//         // Kiểm tra mật khẩu (Cắt khoảng trắng thừa nếu có)
//         if (!user.getPassword().trim().equals(request.getPassword().trim())) {
//             return ResponseEntity
//                     .status(HttpStatus.UNAUTHORIZED)
//                     .body("Mật khẩu không đúng.");
//         }

//         // Không trả password về client để đảm bảo bảo mật
//         user.setPassword(null); 

//         // Tạo response trả về cho Angular
//         AuthResponse response = new AuthResponse();
//         response.setAccessToken("normal_login_token");
//         response.setUser(user);

//         // Trả về JSON thành công
//         return ResponseEntity.ok(response);
//     }

//     /**
//      * API đăng nhập bằng Google
//      */
//     @PostMapping("/google-login")
//     public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {

//         // TODO: Xác thực Google ID Token thật sự ở đây trong môi trường production
//         // Hiện tại xử lý demo dựa trên email mẫu 'admin@example.com' trong Database của bạn
//         String demoUsername = "admin"; 

//         // KHẮC PHỤC: Sử dụng đúng class đối tượng Users (Xóa bỏ Class User của Tomcat cũ)
//         Optional<Users> userOptional = userRepository.findByUsername(demoUsername);

//         if (!userOptional.isPresent()) {
//             return ResponseEntity
//                     .status(HttpStatus.UNAUTHORIZED)
//                     .body("Không tìm thấy người dùng liên kết với tài khoản này.");
//         }

//         Users user = userOptional.get();
//         user.setPassword(null); // Đảm bảo an toàn thông tin

//         AuthResponse response = new AuthResponse();
//         // Giả lập cấp Token Google thành công
//         response.setAccessToken("google_login_token_" + System.currentTimeMillis());
//         response.setUser(user);

//         return ResponseEntity.ok(response);
//     }

//     /**
//      * DTO nhận idToken từ Angular
//      */
//     public static class GoogleLoginRequest {
//         private String idToken;

//         public String getIdToken() {
//             return idToken;
//         }

//         public void setIdToken(String idToken) {
//             this.idToken = idToken;
//         }
//     }
// }