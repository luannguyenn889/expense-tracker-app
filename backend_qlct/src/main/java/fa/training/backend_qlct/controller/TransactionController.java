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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200") // Cho phép Angular tiếp cận API
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // 1. API Tìm kiếm nâng cao kết hợp phân trang (Khớp với getAdvancedSearch của Angular)
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

    // 2. API Xuất báo cáo Excel / PDF động (Khớp với downloadReportFile của Angular)
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
    // Endpoint cho UC32
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

    // Endpoint cho UC31
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(transactionService.getNotifications(userId));
    }
}