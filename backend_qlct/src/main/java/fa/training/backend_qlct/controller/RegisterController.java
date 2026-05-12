package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.UserCreationRequest;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class RegisterController {
    @Autowired
    UserService userService;
//    @PostMapping("/register")
//    public Users createUser(@RequestBody UserCreationRequest request) {
//        return userService.createRequest(request);
//    }
//

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreationRequest request) {
        try {
            // Gọi service để xử lý đăng ký và lưu vào DB
            Users newUser = userService.createRequest(request);

            // Trả về HTTP Status 200 OK (hoặc 201 Created) kèm thông tin user vừa tạo
            return ResponseEntity.ok(newUser);

        } catch (Exception e) {
            // Nếu có lỗi (ví dụ: Email đã tồn tại), trả về lỗi 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
