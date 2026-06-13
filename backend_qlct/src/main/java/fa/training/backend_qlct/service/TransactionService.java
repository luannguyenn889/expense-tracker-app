package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.TransactionRequest;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.respository.TransactionRepository;
import fa.training.backend_qlct.respository.CategoryRepository;
import fa.training.backend_qlct.repository.WalletRepository;
import fa.training.backend_qlct.respository.UserRepository;
import fa.training.backend_qlct.entities.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Transactional
    public Transaction createTransaction(TransactionRequest request, Long userId) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!wallet.getUserId().equals(userId)) {
            throw new RuntimeException("Ví không thuộc về bạn");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền phải lớn hơn 0");
        }

        BigDecimal balanceChange;
        if ("INCOME".equals(request.getType())) {
            balanceChange = request.getAmount();
        } else if ("EXPENSE".equals(request.getType())) {
            balanceChange = request.getAmount().negate();
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Số dư không đủ");
            }
        } else if ("TRANSFER".equals(request.getType())) {
            balanceChange = request.getAmount().negate();
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Số dư không đủ để chuyển");
            }
        } else {
            balanceChange = BigDecimal.ZERO;
        }

        walletRepository.updateBalance(request.getWalletId(), balanceChange);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        transaction.setType(request.getType());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setWalletId(request.getWalletId());
        if ("TRANSFER".equals(request.getType())) {
            transaction.setToWalletId(request.getToWalletId());
        }
        transaction.setUserId(userId);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long id, TransactionRequest request, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền sửa giao dịch này");
        }
        
        if ("TRANSFER".equals(transaction.getType())) {
            throw new RuntimeException("Không thể sửa giao dịch chuyển tiền. Vui lòng xóa và tạo mới.");
        }
        
        if ("INCOME".equals(transaction.getType())) {
            walletRepository.updateBalance(transaction.getWalletId(), transaction.getAmount().negate());
        } else if ("EXPENSE".equals(transaction.getType())) {
            walletRepository.updateBalance(transaction.getWalletId(), transaction.getAmount());
        }
        
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setType(request.getType());
        
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            transaction.setCategoryId(request.getCategoryId());
        } else {
            transaction.setCategoryId(null);
        }
        
        transaction.setWalletId(request.getWalletId());
        
        BigDecimal newBalanceChange;
        if ("INCOME".equals(request.getType())) {
            newBalanceChange = request.getAmount();
        } else {
            newBalanceChange = request.getAmount().negate();
            Wallet wallet = walletRepository.findById(request.getWalletId()).orElseThrow();
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Số dư không đủ");
            }
        }
        walletRepository.updateBalance(request.getWalletId(), newBalanceChange);
        
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa giao dịch này");
        }
        
        Long walletId = transaction.getWalletId();
        Long toWalletId = transaction.getToWalletId();
        
        // HOÀN TÁC SỐ DƯ
        if ("INCOME".equals(transaction.getType())) {
            walletRepository.updateBalance(walletId, transaction.getAmount().negate());
        } else if ("EXPENSE".equals(transaction.getType())) {
            walletRepository.updateBalance(walletId, transaction.getAmount());
        } else if ("TRANSFER".equals(transaction.getType())) {
            // Chuyển tiền: trả lại tiền cho ví gửi, trừ lại tiền từ ví nhận
            walletRepository.updateBalance(walletId, transaction.getAmount());
            if (toWalletId != null) {
                walletRepository.updateBalance(toWalletId, transaction.getAmount().negate());
            }
        }
        
        transactionRepository.deleteById(id);
        
        // KIỂM TRA VÍ CÒN GIAO DỊCH NÀO KHÔNG
        boolean stillHasTransactions = transactionRepository.existsByWalletIdOrToWalletId(walletId, userId);
        
        if (!stillHasTransactions) {
            Wallet wallet = walletRepository.findById(walletId).orElse(null);
            if (wallet != null && "INACTIVE".equals(wallet.getStatus())) {
                wallet.setStatus("ACTIVE");
                walletRepository.save(wallet);
            }
        }
    }
    
    public Page<Transaction> getTransactions(Long userId, LocalDate startDate, LocalDate endDate, 
                                              String type, Long walletId, String query, Pageable pageable) {
        List<String> categoryIds = List.of(""); // default dummy ID to avoid SQL syntax issue on empty IN
        if (query != null && !query.trim().isEmpty()) {
            List<String> foundIds = categoryRepository.findByUserIdAndNameContaining(userId, query)
                    .stream()
                    .map(Categories::getId)
                    .collect(Collectors.toList());
            if (!foundIds.isEmpty()) {
                categoryIds = foundIds;
            }
        }
        return transactionRepository.searchTransactions(userId, startDate, endDate, type, walletId, query, categoryIds, pageable);
    }

    public Transaction getTransactionById(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền truy cập giao dịch này");
        }
        return transaction;
    }

    // phuong thuc boc tach Transactions thanh JSON
    public String extractTransactionJson(String userInput) {
        String prompt = "Bạn là một trợ lý tài chính. Hãy trích xuất thông tin giao dịch từ câu sau: '" + userInput + "'. "
                + "Chỉ trả về MỘT mảng JSON duy nhất, tuyệt đối không giải thích thêm. "
                + "Định dạng JSON: [{\"note\": \"Tên chi tiêu\", \"amount\": Số_tiền_VNĐ_bằng_số, \"category_name\": \"Tên danh mục\"}]. "
                + "Ví dụ: Nếu người dùng nói 'ăn phở 35k', trả về: [{\"note\": \"Ăn phở\", \"amount\": 35000, \"category_name\": \"Ăn uống\"}].";

        // Gọi API Gemini bằng RestTemplate giống như đã làm ở phần trước
        String aiResponse = callGeminiApi(prompt);
        // AI thường trả về chuỗi có bọc ```json ... ```, bạn cần làm sạch nó
        return aiResponse.replace("```json", "").replace("```", "").trim();
    }
   // ham goi API Gemini
    private String callGeminiApi(String prompt) {
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
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi gọi AI: " + e.getMessage());
            return "[]"; // Trả về mảng rỗng để không bị lỗi parse JSON
        }
    }
   //  hàm phụ trợ làm sạch JSON cho AI
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
    // Tao giao dich tu nhap du lieu tu AI
    @Transactional
    public List<Transaction> createTransactionsFromChat(String userInput, Long userId, Long walletId, String username) {
        // 1. Phân giải User
        Users user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }
        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElse(null);
        }
        if (user == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng trong hệ thống");
        }
        Long resolvedUserId = user.getId();

        // 2. Phân giải Ví
        Long finalWalletId = walletId;
        if (finalWalletId == null) {
            List<Wallet> activeWallets = walletRepository.findActiveByUserId(resolvedUserId);
            if (!activeWallets.isEmpty()) {
                finalWalletId = activeWallets.get(0).getId();
            } else {
                List<Wallet> allWallets = walletRepository.findByUserId(resolvedUserId);
                if (!allWallets.isEmpty()) {
                    finalWalletId = allWallets.get(0).getId();
                } else {
                    throw new RuntimeException("Bạn cần tạo ít nhất một ví tài khoản trước khi thực hiện giao dịch bằng AI");
                }
            }
        }

        // 3. Lấy chuỗi JSON từ AI
        String jsonResult = extractTransactionJson(userInput);

        // 4. Chuyển JSON thành danh sách
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> chatTransactions;
        try {
            chatTransactions = mapper.readValue(jsonResult, new TypeReference<List<Map<String, Object>>>(){});
        } catch (Exception e) {
            throw new RuntimeException("AI trả về định dạng không hợp lệ hoặc không thể phân tích cú pháp: " + jsonResult);
        }

        List<Transaction> savedTransactions = new ArrayList<>();

        // 5. Lặp qua danh sách, tìm Category tương ứng và lưu vào DB
        for (Map<String, Object> trx : chatTransactions) {
            String note = (String) trx.get("note");
            
            Object amountObj = trx.get("amount");
            BigDecimal amount = BigDecimal.ZERO;
            if (amountObj instanceof Number) {
                amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                try {
                    amount = new BigDecimal((String) amountObj);
                } catch (Exception e) {
                    amount = BigDecimal.ZERO;
                }
            }

            String categoryName = (String) trx.get("category_name");

            // Tìm Category theo tên
            String categoryId = null;
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                List<Categories> matchingCategories = categoryRepository.findByUserIdAndNameContaining(resolvedUserId, categoryName);
                if (!matchingCategories.isEmpty()) {
                    categoryId = matchingCategories.get(0).getId();
                } else {
                    List<Categories> allCategories = categoryRepository.findByUserIdOrUserIdIsNull(resolvedUserId);
                    for (Categories cat : allCategories) {
                        if (cat.getName().toLowerCase().contains(categoryName.toLowerCase()) ||
                            categoryName.toLowerCase().contains(cat.getName().toLowerCase())) {
                            categoryId = cat.getId();
                            break;
                        }
                    }
                }
            }

            // Tạo request cho Transaction
            TransactionRequest request = new TransactionRequest();
            request.setAmount(amount);
            request.setNote(note);
            request.setTransactionDate(LocalDate.now());
            request.setType("EXPENSE"); // Mặc định là chi tiêu
            request.setCategoryId(categoryId);
            request.setWalletId(finalWalletId);

            // Lưu giao dịch
            Transaction saved = createTransaction(request, resolvedUserId);
            savedTransactions.add(saved);
        }

        return savedTransactions;
    }
}