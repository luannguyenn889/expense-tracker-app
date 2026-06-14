package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.BudgetRequest;
import fa.training.backend_qlct.entities.Budgets;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.repository.BudgetRepository;
import fa.training.backend_qlct.respository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 import fa.training.backend_qlct.dto.response.BudgetProgressResponse;
 import fa.training.backend_qlct.respository.TransactionRepository;
 import java.time.LocalDate;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.ArrayList;
@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public Budgets createBudget(BudgetRequest request, Long userId) throws Exception {
        // 1. Kiem tra quy tac nghiep vu: Trung lap thang/nam
        boolean exists = budgetRepository.existsByCategoryIdAndUserIdAndMonthAndYear(
                request.getCategoryId(), userId, request.getMonth(), request.getYear()
        );

        if (exists) {
            throw new Exception("Danh mục này đã được thiết lập hạn mức trong tháng " + request.getMonth() + "/" + request.getYear());
        }

        // 2. Lay category tu DB
        Categories category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new Exception("Không tìm thấy danh mục!"));

        // 3. Luu Budget
        Budgets budget = new Budgets();
        budget.setCategory(category);
        budget.setUserId(userId);
        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());

        return budgetRepository.save(budget);
    }

    @Autowired
    private TransactionRepository transactionRepository; // Bổ sung inject TransactionRepository

    // Thêm hàm này vào trong class BudgetService
    public List<BudgetProgressResponse> getBudgetProgress(Long userId, Integer month, Integer year) {
        List<Budgets> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        List<BudgetProgressResponse> progressList = new ArrayList<>();

        // Xác định ngày đầu tháng và ngày cuối tháng
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        for (Budgets b : budgets) {
            BudgetProgressResponse dto = new BudgetProgressResponse();
            dto.setCategoryId(b.getCategory().getId());
            dto.setCategoryName(b.getCategory().getName());
            dto.setBudgetAmount(b.getAmount());

            // GỌI HÀM TÍNH BÙ TRỪ MỚI TẠO Ở BƯỚC 1
            BigDecimal netSpend = transactionRepository.calculateNetSpendingByCategoryIdAndDate(
                    userId, b.getCategory().getId(), startDate, endDate);

            // Xử lý logic nếu Thu nhiều hơn Chi (ra số âm) thì đưa về 0 để thanh tiến độ không bị lỗi UI
            Double actual = netSpend != null ? netSpend.doubleValue() : 0.0;
            if (actual < 0) {
                actual = 0.0;
            }

            dto.setActualSpend(actual);

            // Tính phần trăm: (Thực chi / Hạn mức) * 100
            double percent = (actual / b.getAmount()) * 100;
            // Làm tròn 1 chữ số thập phân
            dto.setPercentage(Math.round(percent * 10.0) / 10.0);

            progressList.add(dto);
        }
        return progressList;
    }
}