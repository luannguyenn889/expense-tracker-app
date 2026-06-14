package fa.training.backend_qlct.dto.request;

public class ContributeRequest {
    private Long goalId;
    private Long walletId;
    private Double amount;

    // Getters & Setters
    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}