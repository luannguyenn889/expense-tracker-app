package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.UserCreationRequest;
import fa.training.backend_qlct.dto.request.UserUpdateRequest;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Service la lop chua logic xu ly, tuong tac voi repository de lay du lieu va tra ve cho controller de tra ve cho client
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public Users createRequest(UserCreationRequest request){
       Users users = new Users();
       users.setUsername(request.getUsername());
       users.setPassword(request.getPassword());
       users.setFirstname(request.getFirstname());
       users.setLastname(request.getLastname());
       users.setDob(request.getDob());

       return userRepository.save(users);
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users getUser(String id){
       return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Users updateRequest(String userID,UserUpdateRequest request){
        Users users = getUser(userID);
        users.setFirstname(request.getFirstname());
        users.setLastname(request.getLastname());
        users.setDob(request.getDob());
        users.setPassword(request.getPassword());

        return userRepository.save(users);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // Tim user theo username va password (su dung query thong qua Spring Data JPA cho toi uu)
    public Users findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    // Tim user theo password -> ban khong nen co ham nay, rat mat bao mat va khong thuc te.
}
