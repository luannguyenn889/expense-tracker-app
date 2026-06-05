package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.TransferRequest;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.respository.TransactionRepository;
import fa.training.backend_qlct.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class TransferService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public String transfer(TransferRequest request, Long userId) {
        Wallet fromWallet = walletRepository.findById(request.getFromWalletId()).orElse(null);
        if (fromWallet == null) return "Ví gửi không tồn tại";

        Wallet toWallet = walletRepository.findById(request.getToWalletId()).orElse(null);
        if (toWallet == null) return "Ví nhận không tồn tại";

        if (!fromWallet.getUserId().equals(userId)) return "Ví gửi không thuộc về bạn";
        if (!toWallet.getUserId().equals(userId)) return "Ví nhận không thuộc về bạn";

        if (request.getFromWalletId().equals(request.getToWalletId())) return "Ví gửi và ví nhận không được trùng nhau";

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) return "Số dư không đủ. Hiện có: " + fromWallet.getBalance();

        walletRepository.updateBalance(request.getFromWalletId(), request.getAmount().negate());
        walletRepository.updateBalance(request.getToWalletId(), request.getAmount());

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(request.getTransferDate() != null ? request.getTransferDate() : LocalDate.now());
        transaction.setType("TRANSFER");  
        transaction.setWalletId(request.getFromWalletId());
        transaction.setToWalletId(request.getToWalletId());
        transaction.setUserId(userId);
        

        transactionRepository.save(transaction);

        return "success";
    }
}