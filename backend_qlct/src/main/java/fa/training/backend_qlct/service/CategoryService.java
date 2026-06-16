package fa.training.backend_qlct.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fa.training.backend_qlct.dto.request.CategoryCreationRequest;
import fa.training.backend_qlct.dto.request.CategoryUpdateRequest;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.respository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Thêm danh mục
    public Categories categoryRequest(CategoryCreationRequest request) {

        Categories categories = new Categories();

        categories.setName(request.getName());
        categories.setIcon(request.getIcon());
        categories.setType(request.getType());
        categories.setUserId(request.getUserId());
        categories.setColor(request.getColor());
        categories.setDescription(request.getDescription());
        categories.setMonthlyBudget(request.getMonthlyBudget());

        return categoryRepository.save(categories);
    }

    // Lấy tất cả danh mục
    public List<Categories> getAllUsers() {
        return categoryRepository.findAll();
    }

    // Lấy danh mục theo user
    public List<Categories> getCategoriesByUser(Long userId) {
        return categoryRepository.findByUserIdOrUserIdIsNull(userId);
    }
    // Lay danh muc theo id
    public Categories getCategory(String id){
       return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    // Xoa danh muc theo id
    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }
    // Cap nhat danh muc theo id
    public Categories updateCategoryRequest(String id, CategoryUpdateRequest request){
         Categories categories = getCategory(id);
        categories.setName(request.getName());
        categories.setIcon(request.getIcon());
        categories.setType(request.getType());
        categories.setUserId(request.getUserId());
        categories.setColor(request.getColor());
        categories.setDescription(request.getDescription());
        categories.setMonthlyBudget(request.getMonthlyBudget());
        return categoryRepository.save(categories);
    }
}


