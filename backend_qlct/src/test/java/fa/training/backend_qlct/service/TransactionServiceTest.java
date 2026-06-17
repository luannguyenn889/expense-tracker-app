package fa.training.backend_qlct.service;

import fa.training.backend_qlct.dto.request.TransactionRequest;
import fa.training.backend_qlct.dto.request.TransactionResponse;
import fa.training.backend_qlct.entities.Categories;
import fa.training.backend_qlct.entities.Transaction;
import fa.training.backend_qlct.entities.Users;
import fa.training.backend_qlct.entities.Wallet;
import fa.training.backend_qlct.repository.WalletRepository;
import fa.training.backend_qlct.respository.CategoryRepository;
import fa.training.backend_qlct.respository.TransactionRepository;
import fa.training.backend_qlct.respository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Users testUser;
    private Wallet testWallet1;
    private Wallet testWallet2;
    private Categories testCategory;

    @BeforeEach
    public void setUp() {
        // Create a test user
        Users user = new Users();
        user.setUsername("testuser_" + System.currentTimeMillis());
        user.setPassword("password");
        testUser = userRepository.save(user);

        // Create test wallets
        Wallet w1 = new Wallet();
        w1.setName("Ví chính");
        w1.setBalance(BigDecimal.valueOf(1000000)); // 1,000,000
        w1.setUserId(testUser.getUserId());
        w1.setStatus("ACTIVE");
        testWallet1 = walletRepository.save(w1);

        Wallet w2 = new Wallet();
        w2.setName("Ví phụ");
        w2.setBalance(BigDecimal.valueOf(500000)); // 500,000
        w2.setUserId(testUser.getUserId());
        w2.setStatus("ACTIVE");
        testWallet2 = walletRepository.save(w2);

        // Create a test category
        Categories cat = new Categories();
        cat.setName("Ăn uống");
        cat.setType("EXPENSE");
        cat.setUserId(testUser.getUserId());
        testCategory = categoryRepository.save(cat);
    }

    @Test
    public void testCreateIncomeTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(100000));
        request.setType("INCOME");
        request.setCategoryId(testCategory.getId());
        request.setNote("Nhận lương");

        TransactionResponse response = transactionService.createTransaction(request, testUser.getUserId());
        assertNotNull(response.getTransaction());
        assertEquals("INCOME", response.getTransaction().getType());
        assertEquals(0, response.getTransaction().getAmount().compareTo(BigDecimal.valueOf(100000)));

        Wallet updatedWallet = walletRepository.findById(testWallet1.getId()).orElseThrow();
        assertEquals(0, updatedWallet.getBalance().compareTo(BigDecimal.valueOf(1100000))); // 1,000,000 + 100,000
    }

    @Test
    public void testCreateExpenseTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(200000));
        request.setType("EXPENSE");
        request.setCategoryId(testCategory.getId());
        request.setNote("Ăn trưa");

        TransactionResponse response = transactionService.createTransaction(request, testUser.getUserId());
        assertNotNull(response.getTransaction());
        assertEquals("EXPENSE", response.getTransaction().getType());

        Wallet updatedWallet = walletRepository.findById(testWallet1.getId()).orElseThrow();
        assertEquals(0, updatedWallet.getBalance().compareTo(BigDecimal.valueOf(800000))); // 1,000,000 - 200,000
    }

    @Test
    public void testCreateTransferTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setToWalletId(testWallet2.getId());
        request.setAmount(BigDecimal.valueOf(300000));
        request.setType("TRANSFER");
        request.setNote("Chuyển tiền sang ví phụ");

        TransactionResponse response = transactionService.createTransaction(request, testUser.getUserId());
        assertNotNull(response.getTransaction());
        assertEquals("TRANSFER", response.getTransaction().getType());
        assertEquals(testWallet2.getId(), response.getTransaction().getToWalletId());

        Wallet updatedWallet1 = walletRepository.findById(testWallet1.getId()).orElseThrow();
        Wallet updatedWallet2 = walletRepository.findById(testWallet2.getId()).orElseThrow();

        assertEquals(0, updatedWallet1.getBalance().compareTo(BigDecimal.valueOf(700000)));  // 1,000,000 - 300,000
        assertEquals(0, updatedWallet2.getBalance().compareTo(BigDecimal.valueOf(800000)));  // 500,000 + 300,000
    }

    @Test
    public void testCreateTransaction_InsufficientBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(1500000)); // More than 1,000,000
        request.setType("EXPENSE");
        request.setCategoryId(testCategory.getId());

        assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction(request, testUser.getUserId());
        });
    }

    @Test
    public void testCreateTransaction_InvalidType() {
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(10000));
        request.setType("INVALID_TYPE");

        assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction(request, testUser.getUserId());
        });
    }

    @Test
    public void testCreateTransaction_NegativeAmount() {
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(-10000));
        request.setType("INCOME");

        assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction(request, testUser.getUserId());
        });
    }

    @Test
    public void testUpdateTransaction_SuccessAndJPAFirstLevelCacheFix() {
        // 1. Create a transaction of 200,000 expense
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(200000));
        request.setType("EXPENSE");
        request.setCategoryId(testCategory.getId());
        request.setNote("Ăn tối");

        TransactionResponse response = transactionService.createTransaction(request, testUser.getUserId());
        Transaction txn = response.getTransaction();

        // Balance after creation: 800,000 in DB
        Wallet currentWallet = walletRepository.findById(testWallet1.getId()).orElseThrow();
        assertEquals(0, currentWallet.getBalance().compareTo(BigDecimal.valueOf(800000)));

        // 2. Update the transaction amount to 950,000 (which requires checking if the reverted amount + remaining balance is sufficient)
        // Reverting the old amount (200,000) makes the balance 1,000,000 in DB.
        // The new amount (950,000) is less than 1,000,000, so it should succeed.
        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setWalletId(testWallet1.getId());
        updateRequest.setAmount(BigDecimal.valueOf(950000));
        updateRequest.setType("EXPENSE");
        updateRequest.setCategoryId(testCategory.getId());
        updateRequest.setNote("Được nâng cấp bữa tối sang xịn");

        Transaction updatedTxn = transactionService.updateTransaction(txn.getId(), updateRequest, testUser.getUserId());
        assertNotNull(updatedTxn);
        assertEquals(0, updatedTxn.getAmount().compareTo(BigDecimal.valueOf(950000)));

        Wallet updatedWallet = walletRepository.findById(testWallet1.getId()).orElseThrow();
        assertEquals(0, updatedWallet.getBalance().compareTo(BigDecimal.valueOf(50000))); // 1,000,000 - 950,000
    }

    @Test
    public void testUpdateTransaction_WrongWalletOwner() {
        // Create an expense
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(100000));
        request.setType("EXPENSE");
        request.setCategoryId(testCategory.getId());

        TransactionResponse response = transactionService.createTransaction(request, testUser.getUserId());
        Transaction txn = response.getTransaction();

        // Attempt to update the transaction to use another user's wallet
        // Create another user and wallet
        Users otherUser = new Users();
        otherUser.setUsername("otheruser_" + System.currentTimeMillis());
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        Wallet otherWallet = new Wallet();
        otherWallet.setName("Ví người lạ");
        otherWallet.setBalance(BigDecimal.valueOf(100000));
        otherWallet.setUserId(otherUser.getUserId());
        otherWallet.setStatus("ACTIVE");
        otherWallet = walletRepository.save(otherWallet);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setWalletId(otherWallet.getId());
        updateRequest.setAmount(BigDecimal.valueOf(50000));
        updateRequest.setType("EXPENSE");
        updateRequest.setCategoryId(testCategory.getId());

        TransactionRequest finalUpdateRequest = updateRequest;
        assertThrows(RuntimeException.class, () -> {
            transactionService.updateTransaction(txn.getId(), finalUpdateRequest, testUser.getUserId());
        });
    }

    @Test
    public void testDeleteTransaction_Success() {
        // Create an expense
        TransactionRequest request = new TransactionRequest();
        request.setWalletId(testWallet1.getId());
        request.setAmount(BigDecimal.valueOf(400000));
        request.setType("EXPENSE");
        request.setCategoryId(testCategory.getId());

        TransactionResponse response = transactionService.createTransaction(request, testUser.getUserId());
        Transaction txn = response.getTransaction();

        Wallet currentWallet = walletRepository.findById(testWallet1.getId()).orElseThrow();
        assertEquals(0, currentWallet.getBalance().compareTo(BigDecimal.valueOf(600000)));

        // Delete the transaction
        transactionService.deleteTransaction(txn.getId(), testUser.getUserId());

        // Wallet balance should be reverted back to 1,000,000
        Wallet updatedWallet = walletRepository.findById(testWallet1.getId()).orElseThrow();
        assertEquals(0, updatedWallet.getBalance().compareTo(BigDecimal.valueOf(1000000)));

        assertFalse(transactionRepository.existsById(txn.getId()));
    }
}
