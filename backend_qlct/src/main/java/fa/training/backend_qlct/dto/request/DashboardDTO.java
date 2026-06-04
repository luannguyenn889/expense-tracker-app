package fa.training.backend_qlct.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netCashFlow;

    private List<Map<String, Object>> cashFlows;

    // Mới
    private List<Map<String, Object>> wallets;
    private BigDecimal totalAssets;

    private List<Map<String, Object>> recentTransactions;

    private List<Map<String, Object>> categoryExpenses;

    public DashboardDTO() {
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getNetCashFlow() {
        return netCashFlow;
    }

    public void setNetCashFlow(BigDecimal netCashFlow) {
        this.netCashFlow = netCashFlow;
    }

    public List<Map<String, Object>> getCashFlows() {
        return cashFlows;
    }

    public void setCashFlows(List<Map<String, Object>> cashFlows) {
        this.cashFlows = cashFlows;
    }

    public List<Map<String, Object>> getWallets() {
        return wallets;
    }

    public void setWallets(List<Map<String, Object>> wallets) {
        this.wallets = wallets;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public List<Map<String, Object>> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<Map<String, Object>> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<Map<String, Object>> getCategoryExpenses() {
        return categoryExpenses;
    }

    public void setCategoryExpenses(List<Map<String, Object>> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }
}