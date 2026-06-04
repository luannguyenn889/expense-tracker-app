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

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import fa.training.backend_qlct.entities.Notification;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.respository.NotificationRepository;
import fa.training.backend_qlct.respository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

public BigDecimal getTotalExpenseByMonth(Long userId, int month, int year) {

    BigDecimal total =
                transactionRepository.sumExpenseByMonth(
                        userId,
                        month,
                        year);

        return total != null
                ? total
                : BigDecimal.ZERO;
    }

    public Page<Transaction> searchTransactions(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                                String type, Long walletId, Double minAmount, Double maxAmount, int page, int size) {
        Specification<Transaction> spec = buildSpecification(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
        return transactionRepository.findAll(spec, PageRequest.of(page, size));
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
 
    // [UC30] Xuất báo cáo Excel bằng Apache POI
    public ByteArrayInputStream exportExcel(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                             String type, Long walletId, Double minAmount, Double maxAmount) throws Exception {
        List<Transaction> list = getFilteredList(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
 
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo giao dịch");
 
            // Tạo style cho header
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
                // FIX #1: tx.getId() trả về Long — cần null-check trước khi dùng
                row.createCell(0).setCellValue(tx.getId() != null ? tx.getId() : 0L);
                row.createCell(1).setCellValue(tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : "");
                row.createCell(2).setCellValue(tx.getNote() != null ? tx.getNote() : "Giao dịch");
                row.createCell(3).setCellValue(tx.getType() != null ? tx.getType().toString() : "");
                row.createCell(4).setCellValue(tx.getAmount() != null ? tx.getAmount().doubleValue() : 0.0);
            }
 
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
 
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
 
    // [UC30] Xuất báo cáo PDF bằng iText 7
    public ByteArrayInputStream exportPdf(Long userId, String keyword, LocalDate startDate, LocalDate endDate,
                                           String type, Long walletId, Double minAmount, Double maxAmount) throws Exception {
        List<Transaction> list = getFilteredList(userId, keyword, startDate, endDate, type, walletId, minAmount, maxAmount);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
 
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDocument = new PdfDocument(writer);
 
        // FIX #2: Dùng try-with-resources để đảm bảo Document luôn được đóng
        // kể cả khi xảy ra exception — tránh resource leak
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
        } // document.close() được gọi tự động ở đây
 
        return new ByteArrayInputStream(out.toByteArray());
    }

    // Giả định bạn có bảng Notification, thêm các method (cho UC31)
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        n.setRead(true);
        notificationRepository.save(n);
    }
   
}