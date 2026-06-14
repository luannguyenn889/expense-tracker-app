package fa.training.backend_qlct.dto.request;

import java.time.LocalDate;

public class SavingGoalRequest {
    private String name;
    private Double targetAmount;
    private LocalDate targetDate;

    // ==================== Getters & Setters ====================
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(Double targetAmount) { this.targetAmount = targetAmount; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
}