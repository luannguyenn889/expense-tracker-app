package fa.training.backend_qlct.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_user_id", columnList = "user_id"),
    @Index(name = "idx_transaction_category_id", columnList = "category_id"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    private String note;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private String type;

    @Column(name = "category_id")
    private String categoryId;  

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "to_wallet_id")
    private Long toWalletId;  // Chỉ dùng cho TRANSFER

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id", insertable = false, updatable = false)
    private Wallet toWallet;

    // Constructor
    public Transaction() {}

    // Getter & Setter
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id;
    }

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

    public Long getUserId() { 
        return userId; 
    }

    public void setUserId(Long userId) { 
        this.userId = userId; 
    }
}