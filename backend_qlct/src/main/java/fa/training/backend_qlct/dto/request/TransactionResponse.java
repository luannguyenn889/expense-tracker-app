package fa.training.backend_qlct.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import fa.training.backend_qlct.entities.Transaction;

public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String note;
    private LocalDate transactionDate;
    private String type;
    private String categoryId;
    private Long walletId;
    private String walletName;
    private Long toWalletId;
    private String toWalletName;
    private Long userId;

    // Thêm 2 field còn thiếu
    private String alertMessage;
    private String alertType;

    public TransactionResponse() {}

    // Thêm method setTransaction() để map từ entity sang DTO
    public void setTransaction(Transaction t) {
        this.id = t.getId();
        this.amount = t.getAmount();
        this.note = t.getNote();
        this.transactionDate = t.getTransactionDate();
        this.type = t.getType();
        this.categoryId = t.getCategoryId();
        this.walletId = t.getWalletId();
        this.toWalletId = t.getToWalletId();
        this.userId = t.getUserId();
    }

    // Thêm method getTransaction() để dùng trong AI service
    public Transaction getTransaction() {
        Transaction t = new Transaction();
        t.setId(this.id);
        t.setAmount(this.amount);
        t.setNote(this.note);
        t.setTransactionDate(this.transactionDate);
        t.setType(this.type);
        t.setCategoryId(this.categoryId);
        t.setWalletId(this.walletId);
        t.setToWalletId(this.toWalletId);
        t.setUserId(this.userId);
        return t;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public Long getToWalletId() { return toWalletId; }
    public void setToWalletId(Long toWalletId) { this.toWalletId = toWalletId; }

    public String getToWalletName() { return toWalletName; }
    public void setToWalletName(String toWalletName) { this.toWalletName = toWalletName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
}