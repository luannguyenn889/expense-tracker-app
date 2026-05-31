package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.UserCreationRequest;
import fa.training.backend_qlct.dto.request.UserUpdateRequest;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// Controller la lop chua cac endpoint,
// nhan request tu client, goi service de xu ly va tra ve response cho client
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserControllers {
    @Autowired
    private UserService userService;

    @PostMapping
    public Users createUser(@RequestBody UserCreationRequest request) {
        return userService.createRequest(request);
    }

    @GetMapping
    public List<Users> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public Users users(@PathVariable("userId") String userId){
        return userService.getUser(userId);
    }
    
    @PutMapping("/{userId}")
    public Users updateRequest(@PathVariable("userId") String userId, @RequestBody UserUpdateRequest request) {
         return userService.updateRequest(userId, request);
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return "User has been deleted!";
    }
}

