package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.LoginRequest;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Hoac de "http://localhost:4200" neu ban chi muon Angular goi
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1. Tim user bang username
            Users user = userService.findByUsername(request.getUsername());

            // 2. Kiem tra xem user co ton tai khong va mat khau co khop khong
            // (Trong thuc te, can su dung PasswordEncoder nhu BCrypt de check mat khau da ma hoa)
            if (user != null && user.getPassword().equals(request.getPassword())) {
                
                // 3. Neu dung -> Tra ve thong tin user (Hoac co the tra ve Token/JWT trong tuong lai)
                return ResponseEntity.ok(user);
                
            } else {
                // 4. Neu sai -> Tra ve thong bao loi 401 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }

        } catch (Exception e) {
            // 5. Loi he thong xuong day
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
