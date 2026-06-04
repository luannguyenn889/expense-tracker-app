package fa.training.backend_qlct.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {
    private BigDecimal amount;
    private String note;
    private LocalDate transactionDate;
    private String type;
    private String categoryId;
    private Long walletId;
    private Long toWalletId;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getToWalletId() { 
        return toWalletId; 
    }

    public void setToWalletId(Long toWalletId) { 
        this.toWalletId = toWalletId; 
    }
}