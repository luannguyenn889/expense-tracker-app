package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.*;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.service.AuthService;
import fa.training.backend_qlct.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    // --- GOOGLE LOGIN ---
    @PostMapping("/google-login")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            // Lấy token từ client gửi lên (Angular, React, vv)
            String googleToken = request.getIdToken(); 
            
            // Xử lý login qua service
            AuthResponse response = authService.processGoogleLogin(googleToken);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google login failed: " + e.getMessage());
        }
    }

    // --- NORMAL LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Users user = userService.findByUsername(request.getUsername());
            if (user != null && user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
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

    // Update Users
    @PutMapping("/update/{userId}")

     public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateRequest request) {
        try{
            Users newUser = userService.updateRequest(userId, request);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
     }


}
