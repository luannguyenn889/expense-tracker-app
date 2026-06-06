package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.WalletRequest;
import fa.training.backend_qlct.dto.response.WalletResponse;
import fa.training.backend_qlct.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // Tạo ví mới
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @RequestBody WalletRequest request,
            @RequestParam Long userId) {
        WalletResponse response = walletService.createWallet(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Lấy tổng số dư
    @GetMapping
    public ResponseEntity<List<WalletResponse>> getUserWallets(@RequestParam Long userId) {
        List<WalletResponse> wallets = walletService.getUserWallets(userId);
        return ResponseEntity.ok(wallets);
    }

    // Cập nhật ví
    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> updateWallet(
            @PathVariable Long id,
            @RequestBody WalletRequest request,
            @RequestParam Long userId) {
        WalletResponse response = walletService.updateWallet(id, request, userId);
        return ResponseEntity.ok(response);
    }

    // Xóa ví
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(
            @PathVariable Long id,
            @RequestParam Long userId) {
        walletService.deleteWallet(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/has-transactions")
    public ResponseEntity<Map<String, Boolean>> hasTransactions(
            @PathVariable Long id,
            @RequestParam Long userId) {
        boolean hasTransactions = walletService.hasTransactions(id, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasTransactions", hasTransactions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<WalletResponse>> getAllWalletsForHistory(@RequestParam Long userId) {
        List<WalletResponse> wallets = walletService.getAllWalletsForHistory(userId);
        return ResponseEntity.ok(wallets);
    }
}