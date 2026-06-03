package fa.training.backend_qlct.service;


import fa.training.backend_qlct.dto.request.CategoryCreationRequest;
import fa.training.backend_qlct.dto.request.CategoryUpdateRequest;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.respository.CategoryRepository;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CategoryService {
     @Autowired
     private CategoryRepository categoryRepository;

     // Insert Category in Database
     public Categories categoryRequest(CategoryCreationRequest request){
         Categories categories = new Categories();
         categories.setName(request.getName());
         categories.setIcon(request.getIcon());
         categories.setType(request.getType());
         categories.setUserId(request.getUserId()); // Thiết lập userId của người dùng
         return categoryRepository.save(categories);

     }
    public List<Categories> getAllUsers() {
        return categoryRepository.findAll();
    }

    public List<Categories> getCategoriesByUser(Long userId) {
        return categoryRepository.findByUserIdOrUserIdIsNull(userId); // Lấy danh sách category theo userId hoặc mặc định hệ thống
    }

    public Categories getCategory(String id){
       return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }
    public Categories updateCategoryRequest(String id, CategoryUpdateRequest request){
         Categories categories = getCategory(id);
        categories.setName(request.getName());
        categories.setIcon(request.getIcon());
        categories.setType(request.getType());
        categories.setUserId(request.getUserId()); // Cập nhật userId
        return categoryRepository.save(categories);
    }


}
