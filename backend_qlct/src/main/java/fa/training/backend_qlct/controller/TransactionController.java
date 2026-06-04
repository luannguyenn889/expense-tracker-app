package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.TransactionRequest;
import fa.training.backend_qlct.dto.request.TransferRequest;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.service.TransactionService;
import fa.training.backend_qlct.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam Long userId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Transaction> transactions = transactionService.getTransactions(
            userId, startDate, endDate, type, walletId, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest request,
                                                @RequestParam Long userId) {
        try {
            Object result = transactionService.createTransaction(request, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id,
                                                @RequestBody TransactionRequest request,
                                                @RequestParam Long userId) {
        try {
            Transaction updated = transactionService.updateTransaction(id, request, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody TransferRequest request,
                                                         @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = transferService.transfer(request, userId);
            if ("success".equals(result)) {
                response.put("success", true);
                response.put("message", "Chuyển tiền thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id,
                                                @RequestParam Long userId) {
        try {
            transactionService.deleteTransaction(id, userId);
            return ResponseEntity.ok().body("Xóa giao dịch thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}