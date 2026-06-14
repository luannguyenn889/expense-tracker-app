package fa.training.backend_qlct.dto.response;

public class BudgetProgressResponse {
    private String categoryId;
    private String categoryName;
    private Double budgetAmount;
    private Double actualSpend;
    private Double percentage;

    // Getters & Setters
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Double getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(Double budgetAmount) { this.budgetAmount = budgetAmount; }

    public Double getActualSpend() { return actualSpend; }
    public void setActualSpend(Double actualSpend) { this.actualSpend = actualSpend; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}