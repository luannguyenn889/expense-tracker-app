package fa.training.backend_qlct.dto.response;

import fa.training.backend_qlct.entities.Transaction;

public class TransactionResponse {
    private Transaction transaction;
    private String alertMessage;
    private String alertType; // "WARNING" (Vàng) hoặc "DANGER" (Đỏ)

    // Getters & Setters
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
}