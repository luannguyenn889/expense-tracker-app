package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.ContributeRequest;
import fa.training.backend_qlct.dto.request.SavingGoalRequest;
import fa.training.backend_qlct.entities.SavingGoals;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.repository.SavingGoalRepository;
import fa.training.backend_qlct.repository.WalletRepository;
import fa.training.backend_qlct.respository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class SavingGoalService {

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public SavingGoals createSavingGoal(SavingGoalRequest request, Long userId) throws Exception {
        // Quy tắc nghiệp vụ 1: Ngày hoàn thành phải lớn hơn ngày hiện tại
        if (request.getTargetDate() == null || !request.getTargetDate().isAfter(LocalDate.now())) {
            throw new Exception("Ngày dự kiến hoàn thành phải lớn hơn ngày hiện tại.");
        }

        if (request.getTargetAmount() == null || request.getTargetAmount() <= 0) {
            throw new Exception("Số tiền mục tiêu phải lớn hơn 0.");
        }

        SavingGoals goal = new SavingGoals();
        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());

        // Quy tắc nghiệp vụ 2: Số tiền tích lũy ban đầu là 0
        goal.setCurrentAmount(0.0);

        goal.setTargetDate(request.getTargetDate());
        goal.setUserId(userId);

        return savingGoalRepository.save(goal);
    }

    @Transactional(rollbackFor = Exception.class) // Đảm bảo đồng bộ 3 bảng
    public SavingGoals contributeToGoal(ContributeRequest request, Long userId) throws Exception {
        // 1. Kiểm tra Mục tiêu và Ví
        SavingGoals goal = savingGoalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new Exception("Không tìm thấy mục tiêu tiết kiệm!"));

        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new Exception("Không tìm thấy ví nguồn!"));

        // 2. Chuyển đổi kiểu dữ liệu Double (của Form) sang BigDecimal (của DB)
        BigDecimal contributeAmount = BigDecimal.valueOf(request.getAmount());

        // 3. Quy tắc nghiệp vụ: Số dư phải lớn hơn hoặc bằng số tiền nạp
        if (wallet.getBalance().compareTo(contributeAmount) < 0) {
            throw new Exception("Số dư trong ví không đủ để thực hiện giao dịch!");
        }

        // 4. Trừ tiền ở Ví nguồn (Dùng hàm negate() để chuyển thành số âm)
        walletRepository.updateBalance(wallet.getId(), contributeAmount.negate());

        // 5. Cộng tiền vào Mục tiêu
        goal.setCurrentAmount(goal.getCurrentAmount() + request.getAmount());
        savingGoalRepository.save(goal);

        // 6. Ghi log Giao dịch loại "SAVING"
        Transaction txn = new Transaction();
        txn.setAmount(contributeAmount);
        txn.setNote("Nạp tiền vào quỹ: " + goal.getName());
        txn.setTransactionDate(LocalDate.now());
        txn.setType("SAVING");
        txn.setWalletId(wallet.getId());
        txn.setUserId(userId);
        transactionRepository.save(txn);

        return goal;
    }

    // [UC24] SỬA MỤC TIÊU
    public SavingGoals updateSavingGoal(Long id, SavingGoalRequest request) throws Exception {
        SavingGoals goal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy mục tiêu tiết kiệm!"));

        if (request.getTargetDate() == null || !request.getTargetDate().isAfter(LocalDate.now())) {
            throw new Exception("Ngày dự kiến hoàn thành phải lớn hơn ngày hiện tại.");
        }
        if (request.getTargetAmount() == null || request.getTargetAmount() <= 0) {
            throw new Exception("Số tiền mục tiêu phải lớn hơn 0.");
        }

        // Chặn không cho sửa mục tiêu thấp hơn số tiền đã nạp
        if (request.getTargetAmount() < goal.getCurrentAmount()) {
            throw new Exception("Số tiền mục tiêu không được nhỏ hơn số tiền đã tích lũy (" + goal.getCurrentAmount() + "đ).");
        }

        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());

        return savingGoalRepository.save(goal);
    }

    // [UC25] XÓA MỤC TIÊU (VÀ HOÀN TIỀN NẾU CÓ)
    @Transactional(rollbackFor = Exception.class)
    public void deleteSavingGoal(Long id, Long refundWalletId, Long userId) throws Exception {
        SavingGoals goal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy mục tiêu tiết kiệm!"));

        // Nếu quỹ đang có tiền và người dùng có chọn ví để hoàn lại
        if (goal.getCurrentAmount() > 0) {
            if (refundWalletId == null) {
                // Nếu người dùng chọn "Xóa trắng", không làm gì thêm với ví.
                // (Tiền bay hơi luôn theo yêu cầu mặc định là 0).
            } else {
                // Nếu chọn ví hoàn tiền
                Wallet wallet = walletRepository.findById(refundWalletId)
                        .orElseThrow(() -> new Exception("Không tìm thấy ví để hoàn tiền!"));

                BigDecimal refundAmount = BigDecimal.valueOf(goal.getCurrentAmount());

                // Cộng lại tiền vào ví
                walletRepository.updateBalance(wallet.getId(), refundAmount);

                // Ghi log Giao dịch loại "REFUND" (Hoàn tiền)
                Transaction txn = new Transaction();
                txn.setAmount(refundAmount);
                txn.setNote("Hoàn tiền từ việc xóa quỹ: " + goal.getName());
                txn.setTransactionDate(LocalDate.now());
                txn.setType("REFUND"); // Bạn có thể đổi thành "INCOME" nếu muốn
                txn.setWalletId(wallet.getId());
                txn.setUserId(userId);
                transactionRepository.save(txn);
            }
        }

        // Cuối cùng: Xóa quỹ khỏi DB
        savingGoalRepository.delete(goal);
    }
}