package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.BudgetRequest;
import fa.training.backend_qlct.entities.Budgets;
import fa.training.backend_qlct.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping("/add")
    public ResponseEntity<?> addBudget(@RequestBody BudgetRequest request, @RequestParam Long userId) {
        try {
            // Trong thuc te, userId nen duoc lay tu Token (Security Context) thay vi @RequestParam
            Budgets savedBudget = budgetService.createBudget(request, userId);
            return ResponseEntity.ok(savedBudget);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // Thêm hàm này vào BudgetController.java
    @GetMapping("/progress")
    public ResponseEntity<?> getProgress(
            @RequestParam Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        try {
            return ResponseEntity.ok(budgetService.getBudgetProgress(userId, month, year));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}