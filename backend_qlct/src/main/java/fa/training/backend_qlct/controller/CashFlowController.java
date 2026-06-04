package fa.training.backend_qlct.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fa.training.backend_qlct.respository.TransactionRepository;

@RestController
@RequestMapping("/api/dashboard/cashFlow")
@CrossOrigin(origins = "http://localhost:4200")
public class CashFlowController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCashFlow(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "6") int months) {

        List<Map<String, Object>> dbData = transactionRepository.getMonthlyChartData(userId, months);
        Map<String, Map<String, Object>> dbMap = new HashMap<>();

        for (Map<String, Object> row : dbData) {
            // Sửa từ row.get("label") sang đúng tên cột DB trả về là "yearMonth"
            String yearMonth = String.valueOf(row.get("yearMonth") != null ? row.get("yearMonth") : row.get("YEARMONTH"));
            dbMap.put(yearMonth, row);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.of(today.getYear(), today.getMonthValue());
        
        // Chuyển sang format yyyy-MM để đồng bộ so khớp Key
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            String key = month.format(formatter);

            Map<String, Object> item = new HashMap<>();
            
            // Định dạng label hiển thị phía UI Angular mong đợi: "MM/yyyy" hoặc "T" + số tháng
            item.put("label", month.format(DateTimeFormatter.ofPattern("MM/yyyy")));

            if (dbMap.containsKey(key)) {
                Map<String, Object> dbRow = dbMap.get(key);
                item.put("income", getDouble(dbRow, "income"));
                item.put("expense", getDouble(dbRow, "expense"));
            } else {
                item.put("income", 0.0);
                item.put("expense", 0.0);
            }
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    private double getDouble(Map<String, Object> map, String key) {
        if (map == null) return 0;
        Object value = map.get(key);
        if (value == null) {
            value = map.get(key.toUpperCase());
        }
        return value == null ? 0 : ((Number) value).doubleValue();
    }
}