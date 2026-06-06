package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.TransactionRequest;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.respository.TransactionRepository;
import fa.training.backend_qlct.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Transactional
    public Transaction createTransaction(TransactionRequest request, Long userId) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!wallet.getUserId().equals(userId)) {
            throw new RuntimeException("Ví không thuộc về bạn");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền phải lớn hơn 0");
        }

        BigDecimal balanceChange;
        if ("INCOME".equals(request.getType())) {
            balanceChange = request.getAmount();
        } else if ("EXPENSE".equals(request.getType())) {
            balanceChange = request.getAmount().negate();
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Số dư không đủ");
            }
        } else {
            balanceChange = BigDecimal.ZERO;
        }

        walletRepository.updateBalance(request.getWalletId(), balanceChange);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        transaction.setType(request.getType());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setWalletId(request.getWalletId());
        if ("TRANSFER".equals(request.getType())) {
            transaction.setToWalletId(request.getToWalletId());
        }
        transaction.setUserId(userId);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long id, TransactionRequest request, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền sửa giao dịch này");
        }
        
        if ("TRANSFER".equals(transaction.getType())) {
            throw new RuntimeException("Không thể sửa giao dịch chuyển tiền. Vui lòng xóa và tạo mới.");
        }
        
        if ("INCOME".equals(transaction.getType())) {
            walletRepository.updateBalance(transaction.getWalletId(), transaction.getAmount().negate());
        } else if ("EXPENSE".equals(transaction.getType())) {
            walletRepository.updateBalance(transaction.getWalletId(), transaction.getAmount());
        }
        
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setType(request.getType());
        
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            transaction.setCategoryId(request.getCategoryId());
        } else {
            transaction.setCategoryId(null);
        }
        
        transaction.setWalletId(request.getWalletId());
        
        BigDecimal newBalanceChange;
        if ("INCOME".equals(request.getType())) {
            newBalanceChange = request.getAmount();
        } else {
            newBalanceChange = request.getAmount().negate();
            Wallet wallet = walletRepository.findById(request.getWalletId()).orElseThrow();
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Số dư không đủ");
            }
        }
        walletRepository.updateBalance(request.getWalletId(), newBalanceChange);
        
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa giao dịch này");
        }
        
        Long walletId = transaction.getWalletId();
        Long toWalletId = transaction.getToWalletId();
        
        // HOÀN TÁC SỐ DƯ
        if ("INCOME".equals(transaction.getType())) {
            walletRepository.updateBalance(walletId, transaction.getAmount().negate());
        } else if ("EXPENSE".equals(transaction.getType())) {
            walletRepository.updateBalance(walletId, transaction.getAmount());
        } else if ("TRANSFER".equals(transaction.getType())) {
            // Chuyển tiền: trả lại tiền cho ví gửi, trừ lại tiền từ ví nhận
            walletRepository.updateBalance(walletId, transaction.getAmount());
            if (toWalletId != null) {
                walletRepository.updateBalance(toWalletId, transaction.getAmount().negate());
            }
        }
        
        transactionRepository.deleteById(id);
        
        // KIỂM TRA VÍ CÒN GIAO DỊCH NÀO KHÔNG
        boolean stillHasTransactions = transactionRepository.existsByWalletIdOrToWalletId(walletId, userId);
        
        if (!stillHasTransactions) {
            Wallet wallet = walletRepository.findById(walletId).orElse(null);
            if (wallet != null && "INACTIVE".equals(wallet.getStatus())) {
                wallet.setStatus("ACTIVE");
                walletRepository.save(wallet);
            }
        }
    }
    
    public Page<Transaction> getTransactions(Long userId, LocalDate startDate, LocalDate endDate, 
                                              String type, Long walletId, Pageable pageable) {
        return transactionRepository.searchTransactions(userId, startDate, endDate, type, walletId, pageable);
    }
}