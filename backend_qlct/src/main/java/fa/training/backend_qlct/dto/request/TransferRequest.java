package fa.training.backend_qlct.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferRequest {
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private String note;
    private LocalDate transferDate;

    public Long getFromWalletId() { 
        return fromWalletId; 
    }

    public void setFromWalletId(Long fromWalletId) { 
        this.fromWalletId = fromWalletId; 
    }

    public Long getToWalletId() { 
        return toWalletId; 
    }

    public void setToWalletId(Long toWalletId) { 
        this.toWalletId = toWalletId; 
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

    public LocalDate getTransferDate() { 
        return transferDate; 
    }

    public void setTransferDate(LocalDate transferDate) { 
        this.transferDate = transferDate; 
    }
}