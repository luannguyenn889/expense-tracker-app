package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.WalletRequest;
import fa.training.backend_qlct.dto.response.WalletResponse;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public WalletResponse createWallet(WalletRequest request, Long userId) {
        Wallet wallet = new Wallet();
        wallet.setName(request.getName());
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        wallet.setDescription(request.getDescription());
        wallet.setUserId(userId);

        Wallet saved = walletRepository.save(wallet);
        return convertToResponse(saved);
    }

    public List<WalletResponse> getUserWallets(Long userId) {
        return walletRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalBalance(Long userId) {
        return walletRepository.findByUserId(userId)
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

    public void deleteWallet(Long id, Long userId) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        walletRepository.delete(wallet);
    }

    private WalletResponse convertToResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setName(wallet.getName());
        response.setBalance(wallet.getBalance());
        response.setDescription(wallet.getDescription());
        response.setCreatedAt(wallet.getCreatedAt());
        return response;
    }
}