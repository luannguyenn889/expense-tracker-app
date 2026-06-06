package fa.training.backend_qlct.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// FIX #1: Đổi từ GenerationType.UUID + String sang IDENTITY + Long
// để khớp với kiểu bigint(20) trong database schema.
// Lỗi cũ khiến INSERT thất bại và JOIN với transactions không ra kết quả.
@Entity
@Table(name = "categories")
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // FIX: UUID → IDENTITY
    private Long id; // FIX: String → Long

    private String name;

    private String icon;

    private String type; // EXPENSE | INCOME

    @Column(name = "user_id")
    private Long userId;

    private String color;

    @Column(length = 500)
    private String description;

    @Column(name = "monthly_budget")
    private Double monthlyBudget;

    // ==================== Getters & Setters ====================

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }
}