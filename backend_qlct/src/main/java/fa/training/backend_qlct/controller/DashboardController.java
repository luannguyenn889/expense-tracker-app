package fa.training.backend_qlct.controller;

import java.time.LocalDate;
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
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private TransactionRepository transactionRepository;

    // --- ENDPOINT 1: LẤY DỮ LIỆU TỔNG QUAN (Giữ nguyên luồng cũ của bạn) ---
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "6") int months) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 1. TÍNH TỔNG QUAN THU CHI
            Map<String, Object> summary = transactionRepository.getDashboardSummary(userId, months);
            double totalIncome = getSafeDouble(summary, "totalIncome");
            double totalExpense = getSafeDouble(summary, "totalExpense");

            response.put("totalIncome", totalIncome);
            response.put("totalExpense", totalExpense);
            response.put("netCashFlow", totalIncome - totalExpense);
            response.put("incomeChange", 0);  
            response.put("expenseChange", 0);
            response.put("spendingAlerts", new ArrayList<>());

            // 2. XỬ LÝ ĐỒ THỊ THU CHI THEO THÁNG (Biểu đồ cột)
            List<Map<String, Object>> dbData = transactionRepository.getMonthlyChartData(userId, months);
            Map<String, Map<String, Object>> dataMap = new HashMap<>();
            if (dbData != null) {
                for (Map<String, Object> row : dbData) {
                    String key = getSafeString(row, "yearMonth");
                    if (!key.isEmpty()) dataMap.put(key, row);
                }
            }

            List<Map<String, Object>> cashFlows = new ArrayList<>();
            LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
            java.time.format.DateTimeFormatter keyFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");

            for (int i = months - 1; i >= 0; i--) {
                LocalDate month = currentMonth.minusMonths(i);
                String key = month.format(keyFormatter); 
                Map<String, Object> item = new HashMap<>();
                item.put("label", "T" + month.getMonthValue()); 

                if (dataMap.containsKey(key)) {
                    Map<String, Object> row = dataMap.get(key);
                    item.put("income", getSafeDouble(row, "income"));
                    item.put("expense", getSafeDouble(row, "expense"));
                } else {
                    item.put("income", 0.0);
                    item.put("expense", 0.0);
                }
                cashFlows.add(item);
            }
            response.put("cashFlows", cashFlows);

            // 3. DANH SÁCH VÍ & TỔNG TÀI SẢN
            List<Map<String, Object>> wallets = transactionRepository.getWallets(userId);
            double totalAssets = 0;
            List<Map<String, Object>> normalWallets = new ArrayList<>();
            if (wallets != null) {
                for (Map<String, Object> w : wallets) {
                    Map<String, Object> nw = new HashMap<>();
                    nw.put("name", getSafeString(w, "name"));
                    double bal = getSafeDouble(w, "balance");
                    nw.put("balance", bal);
                    totalAssets += bal;
                    normalWallets.add(nw);
                }
            }
            response.put("wallets", normalWallets);
            response.put("totalAssets", totalAssets);

            // 4. GIAO DỊCH GẦN ĐÂY
            List<Map<String, Object>> recentTx = transactionRepository.getRecentTransactions(userId);
            List<Map<String, Object>> normalTx = new ArrayList<>();
            if (recentTx != null) {
                for (Map<String, Object> t : recentTx) {
                    Map<String, Object> nt = new HashMap<>();
                    nt.put("title", getSafeString(t, "title"));
                    double amt = getSafeDouble(t, "amount");
                    String type = getSafeString(t, "type");
                    nt.put("amount", "EXPENSE".equalsIgnoreCase(type) ? -amt : amt);
                    normalTx.add(nt);
                }
            }
            response.put("recentTransactions", normalTx);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- ENDPOINT 2: CHỈ LẤY CHI TIÊU THEO DANH MỤC CHO BIỂU ĐỒ TRÒN ---
    @GetMapping("/expense-category")
    public ResponseEntity<List<Map<String, Object>>> getExpenseCategoryOnly(
            @RequestParam Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            List<Map<String, Object>> catExp = transactionRepository.getExpenseByCategory(userId, month, year);
            List<Map<String, Object>> normalCat = new ArrayList<>();
            if (catExp != null) {
                for (Map<String, Object> c : catExp) {
                    Map<String, Object> nc = new HashMap<>();
                    nc.put("name", getSafeString(c, "name"));
                    nc.put("amount", getSafeDouble(c, "amount"));
                    nc.put("percentage", getSafeDouble(c, "percentage"));
                    nc.put("color", getSafeString(c, "color"));
                    normalCat.add(nc);
                }
            }
            return ResponseEntity.ok(normalCat);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Các hàm bổ trợ ép kiểu an toàn
    private double getSafeDouble(Map<String, Object> map, String key) {
        if (map == null) return 0.0;
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        if (val == null) val = map.get(key.toLowerCase());
        return val == null ? 0.0 : ((Number) val).doubleValue();
    }

    private String getSafeString(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        if (val == null) val = map.get(key.toLowerCase());
        return val == null ? "" : String.valueOf(val).trim();
    }
}