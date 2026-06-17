package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.SavingGoalRequest;
import fa.training.backend_qlct.entities.SavingGoals;
import fa.training.backend_qlct.service.SavingGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/saving-goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    @Autowired
    private fa.training.backend_qlct.repository.SavingGoalRepository savingGoalRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addSavingGoal(@RequestBody SavingGoalRequest request, @RequestParam Long userId) {
        try {
            SavingGoals savedGoal = savingGoalService.createSavingGoal(request, userId);
            return ResponseEntity.ok(savedGoal);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/contribute")
    public ResponseEntity<?> contributeToGoal(@RequestBody fa.training.backend_qlct.dto.request.ContributeRequest request,
                                              @RequestParam Long userId) {
        try {
            SavingGoals updatedGoal = savingGoalService.contributeToGoal(request, userId);
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getSavingGoals(@RequestParam Long userId) {
        return ResponseEntity.ok(savingGoalRepository.findByUserId(userId));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateSavingGoal(@PathVariable Long id, @RequestBody SavingGoalRequest request) {
        try {
            SavingGoals updatedGoal = savingGoalService.updateSavingGoal(id, request);
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSavingGoal(
            @PathVariable Long id,
            @RequestParam(required = false) Long refundWalletId,
            @RequestParam Long userId) {
        try {
            savingGoalService.deleteSavingGoal(id, refundWalletId, userId);
            return ResponseEntity.ok("Xóa mục tiêu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}