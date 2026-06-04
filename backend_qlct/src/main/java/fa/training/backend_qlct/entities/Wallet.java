package fa.training.backend_qlct.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, DELETED
    
    // Constructor
    public Wallet() {
    }

    public Wallet(String name, BigDecimal balance, Long userId, String description) {
        this.name = name;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.userId = userId;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Phương thức kiểm tra
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
    
    public boolean isInactive() {
        return "INACTIVE".equals(this.status);
    }

    // Phương thức business logic
    public void addAmount(BigDecimal amount) {
        if (!isActive()) {
            throw new IllegalStateException("Ví đang ngưng hoạt động, không thể thực hiện giao dịch!");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }
        this.balance = this.balance.add(amount);
    }

    public void subtractAmount(BigDecimal amount) {
        if (!isActive()) {
            throw new IllegalStateException("Ví đang ngưng hoạt động, không thể thực hiện giao dịch!");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư không đủ!");
        }
        this.balance = this.balance.subtract(amount);
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                '}';
    }
}