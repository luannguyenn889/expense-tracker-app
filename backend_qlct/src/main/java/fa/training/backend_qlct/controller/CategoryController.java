package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.CategoryCreationRequest;
import fa.training.backend_qlct.dto.request.CategoryUpdateRequest;
import fa.training.backend_qlct.dto.request.UserCreationRequest;
import fa.training.backend_qlct.dto.request.UserUpdateRequest;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
   @Autowired
    private  CategoryService categoryService;


    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@RequestBody CategoryCreationRequest request) {
        try {
            Categories  newCategory = categoryService.categoryRequest(request);
            return ResponseEntity.ok(newCategory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping
    public List<Categories> getAllCategories(@RequestParam(name = "userId", required = false) Long userId) {
        if (userId != null) {
            return categoryService.getCategoriesByUser(userId); // Lấy danh sách theo userId hoặc hệ thống
        }
        return categoryService.getCategoriesByUser(null); // Chỉ lấy danh mục mặc định của hệ thống
    }
    @DeleteMapping("/delete/{id}")
    public void deleteCategory(@PathVariable("id") String id) {
        categoryService.deleteCategory(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categories> getCategory(@PathVariable("id") String id) {
        try {
            Categories category = categoryService.getCategory(id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") String id, @RequestBody CategoryUpdateRequest request) {
        try{
            Categories categories = categoryService.updateCategoryRequest(id, request);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
