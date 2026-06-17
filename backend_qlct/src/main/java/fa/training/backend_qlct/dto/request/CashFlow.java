package fa.training.backend_qlct.dto.request;
public class CashFlow {
    private String label;       // Tháng 1, Tháng 2...
    private Double income;      // Tổng thu
    private Double expense;     // Tổng chi

    public CashFlow(String label, Double income, Double expense) {
        this.label = label;
        this.income = income;
        this.expense = expense;
    }

    public String getLabel() { return label; }
    public Double getIncome() { return income; }
    public Double getExpense() { return expense; }
}