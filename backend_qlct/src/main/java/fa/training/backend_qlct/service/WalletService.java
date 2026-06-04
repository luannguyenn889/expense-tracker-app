package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.WalletRequest;
import fa.training.backend_qlct.dto.response.WalletResponse;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.respository.TransactionRepository;
import fa.training.backend_qlct.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public WalletResponse createWallet(WalletRequest request, Long userId) {
        Wallet wallet = new Wallet();
        wallet.setName(request.getName());
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        wallet.setDescription(request.getDescription());
        wallet.setUserId(userId);
        wallet.setStatus("ACTIVE");

        Wallet saved = walletRepository.save(wallet);
        return convertToResponse(saved);
    }

    public List<WalletResponse> getUserWallets(Long userId) {
        return walletRepository.findActiveByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getAllWalletsForHistory(Long userId) {
        return walletRepository.findAllByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalBalance(Long userId) {
        return walletRepository.findActiveByUserId(userId)
                .stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public WalletResponse updateWallet(Long id, WalletRequest request, Long userId) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        wallet.setName(request.getName());
        wallet.setDescription(request.getDescription());

        Wallet updated = walletRepository.save(wallet);
        return convertToResponse(updated);
    }

    @Transactional
    public void deleteWallet(Long id, Long userId) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Kiểm tra có giao dịch không
        boolean hasTransactions = transactionRepository.existsByWalletIdOrToWalletId(id, userId);
        
        if (hasTransactions) {
            // Đã có giao dịch, chuyển sang ngưng hoạt động
            wallet.setStatus("INACTIVE");
            walletRepository.save(wallet);
        } else {
            // Chưa có giao dịch, xóa hẳn
            walletRepository.delete(wallet);
        }
    }

    public boolean hasTransactions(Long walletId, Long userId) {
        return transactionRepository.existsByWalletIdOrToWalletId(walletId, userId);
    }

    private WalletResponse convertToResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setName(wallet.getName());
        response.setBalance(wallet.getBalance());
        response.setDescription(wallet.getDescription());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setStatus(wallet.getStatus());
        return response;
    }
}