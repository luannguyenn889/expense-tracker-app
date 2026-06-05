package fa.training.backend_qlct.controller;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fa.training.backend_qlct.dto.request.TransactionRequest;
import fa.training.backend_qlct.dto.request.TransferRequest;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.service.TransactionService;
import fa.training.backend_qlct.service.TransferService;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200") // Cho phép Angular tiếp cận API bảo mật
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransferService transferService;

    // ==========================================
    // TRUY VẤN & TÌM KIẾM NÂNG CAO 
    // ==========================================

    /**
     * API Tìm kiếm nâng cao kết hợp phân trang (Khớp với getAdvancedSearch của Angular)
     */
    @GetMapping
    public ResponseEntity<Page<Transaction>> getAdvancedSearch(
            @RequestParam Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Transaction> result = transactionService.searchTransactions(
                userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount, page, size);
        return ResponseEntity.ok(result);
    }

    // ==========================================
    // THAO TÁC CRUD GIAO DỊCH VÀ CHUYỂN KHOẢN
    // ==========================================

    /**
     * API Thêm mới giao dịch (Thuộc nhánh Trang)
     */
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

    /**
     * API Cập nhật giao dịch theo ID (Thuộc nhánh Trang)
     */
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

    /**
     * API Chuyển tiền liên ví nội bộ (Thuộc nhánh Trang)
     */
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

    /**
     * API Xóa giao dịch theo ID (Thuộc nhánh Trang)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id,
                                               @RequestParam Long userId) {
        try {
            transactionService.deleteTransaction(id, userId);
            return ResponseEntity.ok().body(Map.of("message", "Xóa giao dịch thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // XUẤT FILE & BÁO CÁO THỐNG KÊ
    // ==========================================

    /**
     * API Xuất báo cáo Excel / PDF động (Khớp với downloadReportFile của Angular)
     */
    @GetMapping("/export/{format}")
    public ResponseEntity<InputStreamResource> downloadReportFile(
            @PathVariable String format,
            @RequestParam Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {
        
        try {
            ByteArrayInputStream stream;
            String filename;
            MediaType mediaType;

            if ("excel".equalsIgnoreCase(format)) {
                stream = transactionService.exportExcel(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
                filename = "Bao_Cao_Giao_Dich.xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else if ("pdf".equalsIgnoreCase(format)) {
                stream = transactionService.exportPdf(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
                filename = "Bao_Cao_Giao_Dich.pdf";
                mediaType = MediaType.APPLICATION_PDF;
            } else {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(mediaType)
                    .body(new InputStreamResource(stream));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API So sánh chi tiêu tháng hiện tại với tháng trước
     */
    @GetMapping("/spending-comparison")
    public ResponseEntity<Map<String, Object>> getSpendingComparison(@RequestParam Long userId) {
        LocalDate now = LocalDate.now();
        BigDecimal currentMonth = transactionService.getTotalExpenseByMonth(userId, now.getMonthValue(), now.getYear());
        
        LocalDate lastMonthDate = now.minusMonths(1);
        BigDecimal lastMonth = transactionService.getTotalExpenseByMonth(userId, lastMonthDate.getMonthValue(), lastMonthDate.getYear());
        
        Map<String, Object> response = new HashMap<>();
        response.put("currentMonthTotal", currentMonth);
        response.put("lastMonthTotal", lastMonth);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // THÔNG BÁO HỆ THỐNG
    // ==========================================

    /**
     * API Lấy danh sách các thông báo cảnh báo tài chính
     */
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(transactionService.getNotifications(userId));
    }
}