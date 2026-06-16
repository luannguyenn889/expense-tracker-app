package fa.training.backend_qlct.service;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.respository.CategoryRepository;
import fa.training.backend_qlct.respository.TransactionRepository;
import fa.training.backend_qlct.respository.UserRepository;
import fa.training.backend_qlct.repository.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.function.RequestPredicates.headers;

@Service
public class AiService {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;


    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;
    
    public String getFinancialAdvice(String username) {
        // Tìm kiếm user từ username, nếu không tìm thấy (hoặc là guest) thì lấy user đầu tiên trong DB để phục vụ chạy thử/test
        Users user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

        if (user == null) {
            return "Chào bạn! Hiện tại hệ thống chưa có thông tin người dùng và giao dịch nào. Hãy đăng ký tài khoản và tạo giao dịch để nhận lời khuyên tài chính cá nhân hóa từ AI nhé!";
        }

        Long userId = user.getId();
        String displayName = (user.getFirstname() != null ? user.getFirstname() : "") + " " + (user.getLastname() != null ? user.getLastname() : "");
        displayName = displayName.trim().isEmpty() ? user.getUsername() : displayName.trim();

        // 1. TẤT CẢ VÍ HOẠT ĐỘNG VÀ SỐ DƯ
        List<Wallet> wallets = walletRepository.findActiveByUserId(userId);
        BigDecimal totalBalance = BigDecimal.ZERO;
        StringBuilder walletsInfo = new StringBuilder();
        for (Wallet wallet : wallets) {
            walletsInfo.append(String.format("- Ví %s: %,.0f VNĐ\n", wallet.getName(), wallet.getBalance()));
            totalBalance = totalBalance.add(wallet.getBalance());
        }

        // 2. BẢN ĐỒ DANH MỤC
        List<Categories> categories = categoryRepository.findByUserIdOrUserIdIsNull(userId);
        Map<String, Categories> categoryMap = categories.stream()
                .collect(Collectors.toMap(Categories::getId, c -> c, (c1, c2) -> c1));

        // 3. GIAO DỊCH TRONG THÁNG NÀY (ĐỂ TÍNH TỔNG THU/CHI VÀ THEO DÕI NGÂN SÁCH)
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        List<Transaction> monthlyTxns = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryExpenses = new HashMap<>();

        for (Transaction txn : monthlyTxns) {
            if ("INCOME".equals(txn.getType())) {
                totalIncome = totalIncome.add(txn.getAmount());
            } else if ("EXPENSE".equals(txn.getType())) {
                totalExpense = totalExpense.add(txn.getAmount());
                String catId = txn.getCategoryId();
                if (catId != null) {
                    categoryExpenses.put(catId, categoryExpenses.getOrDefault(catId, BigDecimal.ZERO).add(txn.getAmount()));
                } else {
                    categoryExpenses.put("UNCATEGORIZED", categoryExpenses.getOrDefault("UNCATEGORIZED", BigDecimal.ZERO).add(txn.getAmount()));
                }
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        // 4. TỔNG HỢP CHI TIÊU THEO DANH MỤC & CẢNH BÁO VƯỢT HẠN MỨC NGÂN SÁCH
        StringBuilder categorySummary = new StringBuilder();
        if (categoryExpenses.isEmpty()) {
            categorySummary.append("- Chưa thực hiện khoản chi tiêu nào trong tháng này.\n");
        } else {
            for (Map.Entry<String, BigDecimal> entry : categoryExpenses.entrySet()) {
                String catId = entry.getKey();
                BigDecimal spent = entry.getValue();
                String catName = "Chưa phân loại";
                Double budget = null;

                if (!"UNCATEGORIZED".equals(catId)) {
                    Categories cat = categoryMap.get(catId);
                    if (cat != null) {
                        catName = cat.getName();
                        budget = cat.getMonthlyBudget();
                    }
                }

                if (budget != null && budget > 0) {
                    BigDecimal budgetBD = BigDecimal.valueOf(budget);
                    if (spent.compareTo(budgetBD) > 0) {
                        BigDecimal excess = spent.subtract(budgetBD);
                        categorySummary.append(String.format("- Danh mục %s: Đã chi %,.0f VNĐ (Hạn mức ngân sách: %,.0f VNĐ) -> ⚠️ VƯỢT HẠN MỨC %,.0f VNĐ!\n", catName, spent, budget, excess));
                    } else {
                        categorySummary.append(String.format("- Danh mục %s: Đã chi %,.0f VNĐ (Hạn mức ngân sách: %,.0f VNĐ)\n", catName, spent, budget));
                    }
                } else {
                    categorySummary.append(String.format("- Danh mục %s: Đã chi %,.0f VNĐ (Không giới hạn ngân sách)\n", catName, spent));
                }
            }
        }

        // 5. CHI TIẾT 10 GIAO DỊCH GẦN NHẤT
        List<Transaction> recentTxns = transactionRepository.findTop10ByUserIdOrderByTransactionDateDescIdDesc(userId);
        StringBuilder recentSummary = new StringBuilder();
        if (recentTxns.isEmpty()) {
            recentSummary.append("- Chưa có giao dịch nào được ghi lại.\n");
        } else {
            for (Transaction txn : recentTxns) {
                String typeStr = "Chuyển tiền";
                if ("INCOME".equals(txn.getType())) {
                    typeStr = "Thu nhập";
                } else if ("EXPENSE".equals(txn.getType())) {
                    typeStr = "Chi tiêu";
                }
                String catName = "Chưa phân loại";
                if (txn.getCategoryId() != null) {
                    Categories cat = categoryMap.get(txn.getCategoryId());
                    if (cat != null) {
                        catName = cat.getName();
                    }
                }
                recentSummary.append(String.format("- Ngày %s: %s %,.0f VNĐ (Danh mục: %s) - Ghi chú: %s\n",
                        txn.getTransactionDate(), typeStr, txn.getAmount(), catName, (txn.getNote() != null ? txn.getNote() : "Không có")));
            }
        }

        // 6. XÂY DỰNG PROMPT CÁ CẤU TRÚC
        String prompt = String.format(
                "Đóng vai trò là một chuyên gia tư vấn tài chính cá nhân người Việt thông thái, tận tâm và thân thiện. " +
                "Dưới đây là thông tin tài chính thực tế lấy từ cơ sở dữ liệu của khách hàng %s (tên đăng nhập: %s):\n\n" +
                "💰 Tình hình các ví tài khoản:\n%s" +
                "Tổng tài sản khả dụng: %,.0f VNĐ\n\n" +
                "📊 Thống kê thu chi trong tháng này (từ %s đến %s):\n" +
                "- Tổng thu nhập: %,.0f VNĐ\n" +
                "- Tổng chi tiêu: %,.0f VNĐ\n" +
                "- Thặng dư/thâm hụt (Thu - Chi): %,.0f VNĐ\n\n" +
                "📂 Chi tiết chi tiêu theo danh mục:\n%s\n" +
                "📜 Lịch sử 10 giao dịch gần nhất:\n%s\n" +
                "Hãy phân tích chi tiết dữ liệu thực tế trên và trả về phản hồi bằng tiếng Việt thân thiện gồm 2 phần rõ ràng (ngăn cách bằng dòng trống):\n" +
                "1. Nhận xét tổng quan về tình hình tài chính tháng này (đánh giá xem tỷ lệ thu chi đã tốt chưa, cảnh báo rõ các danh mục đang chi tiêu vượt hạn mức).\n" +
                "2. Đưa ra 3 lời khuyên tài chính cá nhân cụ thể, thực tế và có thể hành động ngay dựa trên hành vi chi tiêu thực tế ở trên để tối ưu hóa ngân sách và tăng số tiền tiết kiệm.\n" +
                "(Lưu ý quan trọng: Vui lòng không sử dụng các cú pháp Markdown tiêu đề phức tạp như các dấu thăng #, vì giao diện người dùng chỉ hiển thị văn bản thô. Hãy dùng dấu xuống dòng và emoji hợp lý để phân tách thông tin dễ đọc).",
                displayName, username, walletsInfo, totalBalance, startDate, endDate, totalIncome, totalExpense, netSavings, categorySummary, recentSummary
        );

        // 7. GỌI API GEMINI (Sử dụng ObjectMapper để build JSON an toàn)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds connect timeout
        factory.setReadTimeout(15000);    // 15 seconds read timeout
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + escapeJson(prompt) + "\" }] }] }";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl + "?key=" + apiKey, request, Map.class
            );

            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi gọi AI: " + e.getMessage());
            return "Hiện tại hệ thống AI đang quá tải hoặc gặp sự cố kết nối, vui lòng thử lại sau nhé!";
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < ' ') {
                        String hex = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(hex.substring(hex.length() - 4));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}

