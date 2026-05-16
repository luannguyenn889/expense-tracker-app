package fa.training.backend_qlct.controller;

import fa.training.backend_qlct.dto.request.WalletRequest;
import fa.training.backend_qlct.dto.response.WalletResponse;
import fa.training.backend_qlct.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // Tạo ví mới (POST)
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @RequestBody WalletRequest request,
            @RequestParam Long userId) {
        WalletResponse response = walletService.createWallet(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Lấy danh sách ví của user (GET)
    @GetMapping
    public ResponseEntity<List<WalletResponse>> getUserWallets(@RequestParam Long userId) {
        List<WalletResponse> wallets = walletService.getUserWallets(userId);
        return ResponseEntity.ok(wallets);
    }

    // Lấy tổng số dư (GET)
    @GetMapping("/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalance(@RequestParam Long userId) {
        BigDecimal total = walletService.getTotalBalance(userId);
        return ResponseEntity.ok(total);
    }

    // Cập nhật ví (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> updateWallet(
            @PathVariable Long id,
            @RequestBody WalletRequest request,
            @RequestParam Long userId) {
        WalletResponse response = walletService.updateWallet(id, request, userId);
        return ResponseEntity.ok(response);
    }

    // Xóa ví (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(
            @PathVariable Long id,
            @RequestParam Long userId) {
        walletService.deleteWallet(id, userId);
        return ResponseEntity.noContent().build();
    }
}