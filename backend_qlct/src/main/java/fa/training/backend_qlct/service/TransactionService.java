package fa.training.backend_qlct.service;
import fa.training.backend_qlct.dto.request.TransactionRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import fa.training.backend_qlct.dto.request.TransactionRequest;
import fa.training.backend_qlct.entities.Notification;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.repository.WalletRepository;
import fa.training.backend_qlct.respository.NotificationRepository;
import fa.training.backend_qlct.respository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;

import java.util.Optional;

import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fa.training.backend_qlct.dto.request.TransactionResponse;
import fa.training.backend_qlct.entities.Budgets;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.entities.SavingGoals;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.repository.BudgetRepository;
import fa.training.backend_qlct.repository.SavingGoalRepository;
import fa.training.backend_qlct.respository.CategoryRepository;
import fa.training.backend_qlct.respository.UserRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // ==========================================
    // NGHIỆP VỤ CRUD & THAY ĐỔI SỐ DƯ VÍ
    // ==========================================
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, 
                              WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {
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

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 3. TẠO RESPONSE ĐỂ CHUẨN BỊ TRẢ VỀ CHO FRONTEND
        TransactionResponse response = new TransactionResponse();
        response.setTransaction(savedTransaction);

        // 4. KIỂM TRA CẢNH BÁO [UC20] (Logic mới thêm vào)
        if ("EXPENSE".equals(request.getType()) && request.getCategoryId() != null) {
            LocalDate date = savedTransaction.getTransactionDate();
            int month = date.getMonthValue();
            int year = date.getYear();

            List<Budgets> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
            Budgets budget = budgets.stream()
                    .filter(b -> b.getCategory().getId().equals(request.getCategoryId()))
                    .findFirst().orElse(null);

            if (budget != null) {
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

                // Lấy tổng chi tiêu của danh mục này trong tháng
                BigDecimal totalSpend = transactionRepository.sumExpenseByCategoryIdAndDate(
                        userId, request.getCategoryId(), startDate, endDate);

                double spend = totalSpend != null ? totalSpend.doubleValue() : 0.0;
                double percent = (spend / budget.getAmount()) * 100;

                if (percent > 100) {
                    response.setAlertMessage("Bạn đã vượt hạn mức chi tiêu!");
                    response.setAlertType("DANGER");
                } else if (percent >= 80) {
                    response.setAlertMessage("Bạn đã tiêu gần hết hạn mức!");
                    response.setAlertType("WARNING");
                }
            }
        }
        return response;
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
        
        // Hoàn tác số dư cũ trước khi áp dụng số tiền mới
        if ("INCOME".equals(transaction.getType())) {
            walletRepository.updateBalance(transaction.getWalletId(), transaction.getAmount().negate());
        } else if ("EXPENSE".equals(transaction.getType())) {
            walletRepository.updateBalance(transaction.getWalletId(), transaction.getAmount());
        }
        
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setType(request.getType());
        
        // SỬA LẠI THÀNH
        if (request.getCategoryId() != null) {
            transaction.setCategoryId(request.getCategoryId());
        } else {
            transaction.setCategoryId(null);
        }
        
        transaction.setWalletId(request.getWalletId());
        
        // Cập nhật số dư mới
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
        
        // HOÀN TÁC SỐ DƯ KHI XÓA
        if ("INCOME".equals(transaction.getType())) {
            walletRepository.updateBalance(walletId, transaction.getAmount().negate());
        } else if ("EXPENSE".equals(transaction.getType())) {
            walletRepository.updateBalance(walletId, transaction.getAmount());
        } else if ("TRANSFER".equals(transaction.getType())) {
            walletRepository.updateBalance(walletId, transaction.getAmount());
            if (toWalletId != null) {
                walletRepository.updateBalance(toWalletId, transaction.getAmount().negate());
            }
        }
        
        transactionRepository.deleteById(id);
        
        // KIỂM TRA VÀ KHÔI PHỤC TRẠNG THÁI VÍ NẾU CẦN
        boolean stillHasTransactions = transactionRepository.existsByWalletIdOrToWalletId(walletId, userId);
        if (!stillHasTransactions) {
            Wallet wallet = walletRepository.findById(walletId).orElse(null);
            if (wallet != null && "INACTIVE".equals(wallet.getStatus())) {
                wallet.setStatus("ACTIVE");
                walletRepository.save(wallet);
            }
        }
    }

    // ==========================================
    // TRUY VẤN & TÌM KIẾM NÂNG CAO (SPECIFICATION)
    // ==========================================

    public BigDecimal getTotalExpenseByMonth(Long userId, int month, int year) {
        BigDecimal total = transactionRepository.sumExpenseByMonth(userId, month, year);
        return total != null ? total : BigDecimal.ZERO;
    }
public Page<TransactionResponse> searchTransactions(
    Long userId, String keyword, LocalDate startDate, LocalDate endDate,
    String type, Long walletId, Double minAmount, Double maxAmount,
    int page, int size) {

    Specification<Transaction> spec = buildSpecification(
            userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);

    Page<Transaction> txPage = transactionRepository.findAll(spec, PageRequest.of(page, size,
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "transactionDate", "id")));

    // Convert sang DTO có walletName
    return txPage.map(this::convert);
}

    private List<Transaction> getFilteredList(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                              String type, Long walletId, Double minAmount, Double maxAmount) {
        Specification<Transaction> spec = buildSpecification(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
        return transactionRepository.findAll(spec);
    }


 private TransactionResponse convert(Transaction tx) {

    TransactionResponse dto = new TransactionResponse();

dto.setId(tx.getId());
    dto.setAmount(tx.getAmount());
    dto.setNote(tx.getNote());
    dto.setTransactionDate(tx.getTransactionDate());
    dto.setType(tx.getType());

    dto.setCategoryId(tx.getCategoryId());

    dto.setWalletId(tx.getWalletId());
    dto.setToWalletId(tx.getToWalletId());

    dto.setUserId(tx.getUserId());

    if (tx.getWalletId() != null) {
        walletRepository.findById(tx.getWalletId())
                .ifPresent(w -> dto.setWalletName(w.getName()));
    }

    if (tx.getToWalletId() != null) {
        walletRepository.findById(tx.getToWalletId())
                .ifPresent(w -> dto.setToWalletName(w.getName()));
    }

    return dto;
}
    // ==========================================
    // XUẤT BÁO CÁO FILE (EXCEL & PDF)
    // ==========================================

    public ByteArrayInputStream exportExcel(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                             String type, Long walletId, Double minAmount, Double maxAmount) throws Exception {
        List<Transaction> list = getFilteredList(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
 
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo giao dịch");
 
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
 
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Ngày", "Nội dung", "Loại", "Số tiền (₫)"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
 
            int rowIdx = 1;
            for (Transaction tx : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(tx.getId() != null ? tx.getId() : 0L);
                row.createCell(1).setCellValue(tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : "");
                row.createCell(2).setCellValue(tx.getNote() != null ? tx.getNote() : "Giao dịch");
                row.createCell(3).setCellValue(tx.getType() != null ? tx.getType().toString() : "");
                row.createCell(4).setCellValue(tx.getAmount() != null ? tx.getAmount().doubleValue() : 0.0);
            }
 
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
 
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
 
    public ByteArrayInputStream exportPdf(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                           String type, Long walletId, Double minAmount, Double maxAmount) throws Exception {
        List<Transaction> list = getFilteredList(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
 
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDocument = new PdfDocument(writer);
 
        try (Document document = new Document(pdfDocument)) {
 
            Paragraph title = new Paragraph("BAO CAO GIAO DICH TAI CHINH")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
 
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 4, 2, 2}))
                    .useAllAvailableWidth();
 
            String[] headers = {"ID", "Ngay", "Noi dung", "Loai", "So tien"};
            for (String h : headers) {
                com.itextpdf.layout.element.Cell headerCell = new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(h))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setPadding(8);
                table.addHeaderCell(headerCell);
            }
 
            for (Transaction tx : list) {
                table.addCell(tx.getId() != null ? tx.getId().toString() : "");
                table.addCell(tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : "");
                table.addCell(tx.getNote() != null ? tx.getNote() : "Giao dich");
                table.addCell(tx.getType() != null ? tx.getType().toString() : "");
                table.addCell(tx.getAmount() != null ? String.format("%,.0f d", tx.getAmount().doubleValue()) : "0 d");
            }
 
            document.add(table);
        }
 
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // THÔNG BÁO (NOTIFICATIONS)
    // ==========================================

    // Chỉ lấy thông báo chưa đọc
public List<Notification> getNotifications(Long userId) {
    return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
}

        @Transactional
        public void markAsRead(Long notificationId) {

            Notification notification =
                    notificationRepository.findById(notificationId)
                    .orElseThrow(() ->
                            new RuntimeException("Thông báo không tồn tại"));

            notification.setIsRead(true);

            notificationRepository.save(notification);
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
    // MỚI - thay vào
    private Specification<Transaction> buildSpecification(Long userId, String keyword,
            LocalDate startDate, LocalDate endDate, String type, Long walletId,
            Double minAmount, Double maxAmount) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (keyword != null && !keyword.trim().isEmpty())
                predicates.add(cb.like(cb.lower(root.get("note")), "%" + keyword.toLowerCase() + "%"));
            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            if (type != null && !type.trim().isEmpty())
                predicates.add(cb.equal(root.get("type"), type));
            if (walletId != null)
                predicates.add(cb.equal(root.get("walletId"), walletId));
            if (minAmount != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), BigDecimal.valueOf(minAmount)));
            if (maxAmount != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), BigDecimal.valueOf(maxAmount)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
            return "ERROR: " + e.getMessage();
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

    // Phân tích và xử lý câu chat từ người dùng (Smart Wallet Resolution + Conversational support)
    @Transactional
    public String processChat(String userInput, Long userId, Long walletId, String username) {
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
            return "Không tìm thấy thông tin người dùng trong hệ thống. Vui lòng đăng nhập lại!";
        }
        Long resolvedUserId = user.getId();

        // 2. Lấy dữ liệu ngữ cảnh tài chính của user
        List<Wallet> wallets = walletRepository.findActiveByUserId(resolvedUserId);
        BigDecimal totalBalance = BigDecimal.ZERO;
        StringBuilder walletsInfo = new StringBuilder();
        for (Wallet wallet : wallets) {
            walletsInfo.append(String.format("- Ví '%s' (ID: %d): %,.0f VNĐ\n", wallet.getName(), wallet.getId(), wallet.getBalance()));
            totalBalance = totalBalance.add(wallet.getBalance());
        }

        List<Categories> categories = categoryRepository.findByUserIdOrUserIdIsNull(resolvedUserId);
        Map<String, Categories> categoryMap = categories.stream()
                .collect(Collectors.toMap(Categories::getId, c -> c, (c1, c2) -> c1));

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        List<Transaction> monthlyTxns = transactionRepository.findByUserIdAndTransactionDateBetween(resolvedUserId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Transaction txn : monthlyTxns) {
            if ("INCOME".equals(txn.getType())) {
                totalIncome = totalIncome.add(txn.getAmount());
            } else if ("EXPENSE".equals(txn.getType())) {
                totalExpense = totalExpense.add(txn.getAmount());
            }
        }

        List<Transaction> recentTxns = transactionRepository.findTop10ByUserIdOrderByTransactionDateDescIdDesc(resolvedUserId);
        StringBuilder recentSummary = new StringBuilder();
        if (recentTxns.isEmpty()) {
            recentSummary.append("- Chưa có giao dịch nào gần đây.\n");
        } else {
            for (Transaction txn : recentTxns) {
                String typeStr = "INCOME".equals(txn.getType()) ? "Thu nhập" : ("EXPENSE".equals(txn.getType()) ? "Chi tiêu" : "Chuyển tiền");
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

        // 2.2. Lấy dữ liệu Hạn mức chi tiêu (Budgets)
        List<Budgets> activeBudgets = budgetRepository.findByUserIdAndMonthAndYear(resolvedUserId, startDate.getMonthValue(), startDate.getYear());
        StringBuilder budgetsInfo = new StringBuilder();
        if (activeBudgets.isEmpty()) {
            budgetsInfo.append("- Chưa thiết lập hạn mức chi tiêu nào cho tháng này.\n");
        } else {
            for (Budgets b : activeBudgets) {
                BigDecimal actualSpend = transactionRepository.sumExpenseByCategoryIdAndDate(
                        resolvedUserId, b.getCategory().getId(), startDate, endDate);
                BigDecimal budgetAmount = BigDecimal.valueOf(b.getAmount());
                BigDecimal actual = actualSpend != null ? actualSpend : BigDecimal.ZERO;
                BigDecimal remaining = budgetAmount.subtract(actual);
                double percent = b.getAmount() > 0 ? (actual.doubleValue() / b.getAmount()) * 100 : 0.0;
                budgetsInfo.append(String.format("- Danh mục '%s': Hạn mức %,.0f VNĐ | Đã chi: %,.0f VNĐ (%.1f%%) | %s: %,.0f VNĐ\n",
                        b.getCategory().getName(),
                        b.getAmount(),
                        actual,
                        percent,
                        remaining.compareTo(BigDecimal.ZERO) >= 0 ? "Còn lại" : "Vượt hạn mức",
                        remaining.abs()));
            }
        }

        // 2.3. Lấy dữ liệu Mục tiêu tiết kiệm (Saving Goals)
        List<SavingGoals> savingGoals = savingGoalRepository.findByUserId(resolvedUserId);
        StringBuilder goalsInfo = new StringBuilder();
        if (savingGoals.isEmpty()) {
            goalsInfo.append("- Chưa thiết lập mục tiêu tiết kiệm nào.\n");
        } else {
            for (SavingGoals goal : savingGoals) {
                double percent = goal.getTargetAmount() > 0 ? (goal.getCurrentAmount() / goal.getTargetAmount()) * 100 : 0.0;
                goalsInfo.append(String.format("- Mục tiêu '%s': Đã tích lũy %,.0f / %,.0f VNĐ (%.1f%%) | Hạn chót: %s\n",
                        goal.getName(),
                        goal.getCurrentAmount(),
                        goal.getTargetAmount(),
                        percent,
                        goal.getTargetDate()));
            }
        }

        // 3. Xây dựng prompt thông minh gửi đến Gemini
        String prompt = String.format(
            "Bạn là một trợ lý tài chính cá nhân người Việt thông minh, tận tâm và thân thiện.\n" +
            "Dưới đây là thông tin tài chính thực tế của khách hàng (tên: %s, username: %s):\n\n" +
            "💰 Tình hình các ví tài khoản:\n%s" +
            "Tổng tài sản hiện tại: %,.0f VNĐ\n\n" +
            "📊 Thống kê thu chi trong tháng này (từ %s đến %s):\n" +
            "- Tổng thu nhập: %,.0f VNĐ\n" +
            "- Tổng chi tiêu: %,.0f VNĐ\n" +
            "- Thặng dư/thâm hụt: %,.0f VNĐ\n\n" +
            "🎯 Hạn mức chi tiêu tháng này (Budgets):\n%s\n" +
            "🏆 Các mục tiêu tiết kiệm hiện có (Saving Goals):\n%s\n" +
            "📜 Lịch sử 10 giao dịch gần nhất nhất:\n%s\n" +
            "Người dùng gửi lời nhắn sau: '%s'\n\n" +
            "Nhiệm vụ của bạn:\n" +
            "Hãy phân loại tin nhắn của người dùng thuộc một trong hai loại sau:\n" +
            "1. 'CHAT': Nếu người dùng đang chào hỏi, hỏi về tình hình tài chính của họ (ví dụ: 'tháng này mình tiêu thế nào', 'tổng số dư là bao nhiêu', 'ăn uống còn bao nhiêu hạn mức', 'quỹ mua xe của mình thế nào'), muốn xin lời khuyên tiết kiệm hoặc trò chuyện thông thường.\n" +
            "2. 'TRANSACTION': Nếu người dùng muốn ghi chép/lưu lại một hoặc nhiều giao dịch mới vừa xảy ra (ví dụ: 'mua cafe 30k', 'ăn sáng 35.000', 'nhận lương 15tr', 'đổ xăng 50k ngày hôm qua').\n\n" +
            "Hãy trả về MỘT chuỗi JSON duy nhất, định dạng chính xác như sau, không có bất kỳ văn bản giải thích nào trước hoặc sau nó:\n" +
            "{\n" +
            "  \"type\": \"CHAT\" hoặc \"TRANSACTION\",\n" +
            "  \"reply\": \"Câu trả lời của bạn gửi cho người dùng (nếu type là CHAT. Hãy trả lời ngắn gọn, thân thiện, mang tính cá nhân hóa cao dựa trên dữ liệu tài chính ở trên, bao gồm cả tình hình hạn mức chi tiêu hay mục tiêu tiết kiệm nếu họ hỏi đến, sử dụng emoji và xuống dòng hợp lý. Không dùng ký tự tiêu đề markdown #)\",\n" +
            "  \"transactions\": [\n" +
            "    {\n" +
            "      \"note\": \"Mô tả ngắn gọn về giao dịch (ví dụ: 'Ăn phở', 'Mua cafe', 'Nhận lương')\",\n" +
            "      \"amount\": Số_tiền_VNĐ_bằng_số,\n" +
            "      \"type\": \"EXPENSE\" hoặc \"INCOME\",\n" +
            "      \"category_name\": \"Tên danh mục gợi ý phù hợp (ví dụ: 'Ăn uống', 'Di chuyển', 'Lương', 'Mua sắm', 'Học tập', ...)\",\n" +
            "      \"wallet_name\": \"Tên ví mà người dùng đề cập (ví dụ: 'Momo', 'Ví chính') hoặc null nếu không đề cập\",\n" +
            "      \"date\": \"Ngày giao dịch theo định dạng YYYY-MM-DD (nếu người dùng đề cập thời gian khác ngày hôm nay, ví dụ 'hôm qua' thì tính ngày thích hợp, ngược lại trả về null)\"\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            (user.getFirstname() != null ? user.getFirstname() : "") + " " + (user.getLastname() != null ? user.getLastname() : ""),
            user.getUsername(),
            walletsInfo.toString(),
            totalBalance,
            startDate,
            endDate,
            totalIncome,
            totalExpense,
            totalIncome.subtract(totalExpense),
            budgetsInfo.toString(),
            goalsInfo.toString(),
            recentSummary.toString(),
            userInput
        );

        // 4. Gọi Gemini
        String aiResponse = callGeminiApi(prompt);
        if (aiResponse == null || aiResponse.trim().isEmpty() || aiResponse.startsWith("ERROR:") || "[]".equals(aiResponse)) {
            return "Không thể kết nối đến Google Gemini API hoặc API Key không hợp lệ. Vui lòng kiểm tra lại kết nối mạng của máy chủ và cài đặt 'gemini.api.key' trong file 'application.properties'.";
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap;
        try {
            String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();
            responseMap = mapper.readValue(cleanJson, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return "Trợ lý AI gặp khó khăn trong việc hiểu câu lệnh của bạn. Bạn có thể gõ rõ hơn (ví dụ: 'ăn trưa 40k').";
        }

        if (responseMap == null) {
            return "Trợ lý AI gặp lỗi xử lý thông tin. Vui lòng thử lại sau!";
        }

        String responseType = (String) responseMap.get("type");
        if ("CHAT".equalsIgnoreCase(responseType)) {
            String reply = (String) responseMap.get("reply");
            return reply != null ? reply : "Chào bạn! Tôi có thể giúp gì cho bạn về tài chính cá nhân?";
        }

        // 5. Xử lý ghi nhận TRANSACTION
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trxList = (List<Map<String, Object>>) responseMap.get("transactions");
        if (trxList == null || trxList.isEmpty()) {
            return "Tôi không tìm thấy thông tin giao dịch nào cần ghi chép trong câu chat của bạn. Bạn hãy gõ rõ ràng hơn nhé (ví dụ: 'mua cafe 35k').";
        }

        List<Transaction> savedTransactions = new ArrayList<>();
        StringBuilder resultMessage = new StringBuilder("🤖 Đã ghi nhận giao dịch thành công:\n");

        for (Map<String, Object> trx : trxList) {
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
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            String categoryName = (String) trx.get("category_name");
            String walletNameFromAi = (String) trx.get("wallet_name");
            String txnType = (String) trx.get("type");
            if (txnType == null || (!"INCOME".equals(txnType) && !"EXPENSE".equals(txnType))) {
                txnType = "EXPENSE";
            }

            String dateStr = (String) trx.get("date");
            LocalDate txnDate = LocalDate.now();
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                try {
                    txnDate = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    // Mặc định hôm nay
                }
            }

            // Giải quyết ví thông minh
            Wallet selectedWallet = null;
            List<Wallet> activeWallets = walletRepository.findActiveByUserId(resolvedUserId);

            // 1. Tìm ví khớp với tên do AI nhận diện
            if (walletNameFromAi != null && !walletNameFromAi.trim().isEmpty()) {
                String normalizedAiName = walletNameFromAi.toLowerCase().trim();
                for (Wallet w : activeWallets) {
                    if (w.getName().toLowerCase().contains(normalizedAiName) ||
                        normalizedAiName.contains(w.getName().toLowerCase())) {
                        selectedWallet = w;
                        break;
                    }
                }
            }

            // 2. Chọn ví từ dropdown UI nếu truyền qua walletId
            if (selectedWallet == null && walletId != null) {
                selectedWallet = activeWallets.stream()
                        .filter(w -> w.getId().equals(walletId))
                        .findFirst()
                        .orElse(null);
            }

            // 3. Nếu là EXPENSE và chưa chọn được ví, tìm ví hoạt động có số dư >= amount (chọn ví nhiều tiền nhất)
            if (selectedWallet == null && "EXPENSE".equals(txnType)) {
                final BigDecimal finalAmount = amount;
                List<Wallet> walletsWithSufficientBalance = activeWallets.stream()
                        .filter(w -> w.getBalance().compareTo(finalAmount) >= 0)
                        .sorted((w1, w2) -> w2.getBalance().compareTo(w1.getBalance()))
                        .collect(Collectors.toList());

                if (!walletsWithSufficientBalance.isEmpty()) {
                    selectedWallet = walletsWithSufficientBalance.get(0);
                }
            }

            // 4. Nếu vẫn chưa chọn được ví, chọn ví hoạt động có số dư cao nhất
            if (selectedWallet == null && !activeWallets.isEmpty()) {
                selectedWallet = activeWallets.stream()
                        .max((w1, w2) -> w1.getBalance().compareTo(w2.getBalance()))
                        .orElse(activeWallets.get(0));
            }

            // 5. Fallback cuối cùng
            if (selectedWallet == null) {
                List<Wallet> allWallets = walletRepository.findByUserId(resolvedUserId);
                if (!allWallets.isEmpty()) {
                    selectedWallet = allWallets.get(0);
                } else {
                    return "Bạn cần tạo ít nhất một ví tài khoản trước khi thực hiện giao dịch bằng AI.";
                }
            }

            // Giải quyết danh mục
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

            if (categoryId == null) {
                List<Categories> allCategories = categoryRepository.findByUserIdOrUserIdIsNull(resolvedUserId);
                for (Categories cat : allCategories) {
                    if (cat.getName().toLowerCase().contains("khác") && cat.getType().equals(txnType)) {
                        categoryId = cat.getId();
                        break;
                    }
                }
                // Nếu vẫn chưa tìm thấy, lấy danh mục đầu tiên có cùng loại giao dịch (INCOME/EXPENSE)
                if (categoryId == null) {
                    for (Categories cat : allCategories) {
                        if (cat.getType().equals(txnType)) {
                            categoryId = cat.getId();
                            break;
                        }
                    }
                }
            }

            // Lưu giao dịch
            TransactionRequest request = new TransactionRequest();
            request.setAmount(amount);
            request.setNote(note != null ? note : categoryName);
            request.setTransactionDate(txnDate);
            request.setType(txnType);
            request.setCategoryId(categoryId);
            request.setWalletId(selectedWallet.getId());
            

            try {
                Transaction saved = createTransaction(request, resolvedUserId).getTransaction();
                savedTransactions.add(saved);

                Wallet updatedWallet = walletRepository.findById(selectedWallet.getId()).orElse(selectedWallet);

                String catDisplayName = "Chưa phân loại";
                if (categoryId != null) {
                    Categories cat = categoryRepository.findById(categoryId).orElse(null);
                    if (cat != null) {
                        catDisplayName = cat.getName();
                    }
                }

                String typeSymbol = "EXPENSE".equals(txnType) ? "💸 Chi tiêu" : "💰 Thu nhập";
                resultMessage.append(String.format("- %s: %,.0f VNĐ - '%s' (%s) ghi vào ví '%s' (Số dư mới: %,.0f VNĐ)\n",
                        typeSymbol, amount, request.getNote(), catDisplayName, updatedWallet.getName(),
                        updatedWallet.getBalance()));
            } catch (Exception e) {
                resultMessage.append(String.format("- ⚠️ Lỗi khi ghi nhận '%s': %s\n", note != null ? note : categoryName, e.getMessage()));
            }
        }

        if (savedTransactions.isEmpty()) {
            return "Không thể lưu giao dịch từ câu chat. Lý do: Số dư trong tất cả các ví đều không đủ hoặc thông tin giao dịch không hợp lệ.";
        }

        return resultMessage.toString();
    }

    @Deprecated
    public List<Transaction> createTransactionsFromChat(String userInput, Long userId, Long walletId, String username) {
        return new ArrayList<>();
    }
    
}
