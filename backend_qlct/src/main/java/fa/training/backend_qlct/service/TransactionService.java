package fa.training.backend_qlct.service;

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

import fa.training.backend_qlct.dto.request.TransactionResponse;

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

    public Page<Transaction> searchTransactions(
        Long userId,
        String keyword,
        LocalDate startDate,
        LocalDate endDate,
        String type,
        Long walletId,
        Double minAmount,
        Double maxAmount,
        int page,
        int size) {

    Specification<Transaction> spec =
            buildSpecification(
                    userId,
                    keyword,
                    startDate,
                    endDate,
                    type,
                    walletId,
                    minAmount,
                    maxAmount
            );

    return transactionRepository.findAll(
            spec,
            PageRequest.of(page, size)
    );
}

    private List<Transaction> getFilteredList(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                              String type, Long walletId, Double minAmount, Double maxAmount) {
        Specification<Transaction> spec = buildSpecification(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
        return transactionRepository.findAll(spec);
    }

    private Specification<Transaction> buildSpecification(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                                           String type, Long walletId, Double minAmount, Double maxAmount) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("note")), "%" + keyword.toLowerCase() + "%"));
            }
            if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            if (type != null && !type.trim().isEmpty()) predicates.add(cb.equal(root.get("type"), type));
            if (walletId != null) predicates.add(cb.equal(root.get("walletId"), walletId));
            if (minAmount != null) predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), BigDecimal.valueOf(minAmount)));
            if (maxAmount != null) predicates.add(cb.lessThanOrEqualTo(root.get("amount"), BigDecimal.valueOf(maxAmount)));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        n.setRead(true);
        notificationRepository.save(n);
    }
}