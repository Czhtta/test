package com.comp5348.bank.service;

import com.comp5348.bank.dto.TransferRequest;
import com.comp5348.bank.dto.TransferResponse;
import com.comp5348.bank.entity.Account;
import com.comp5348.bank.entity.TransactionRecord;
import com.comp5348.bank.repository.AccountRepository;
import com.comp5348.bank.repository.TransactionRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bank service for handling transfers and refunds
 */
@Service
public class BankService {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    /**
     * Process payment from customer to store
     */
    @Transactional
    public TransferResponse processPayment(TransferRequest request) {
        String transactionId = UUID.randomUUID().toString();
        logger.info("Processing payment {} for order {}: {} -> {} amount: {}",
                transactionId, request.getOrderId(), request.getFromAccount(),
                request.getToAccount(), request.getAmount());

        // Create transaction record
        TransactionRecord record = new TransactionRecord();
        record.setTransactionId(transactionId);
        record.setFromAccount(request.getFromAccount());
        record.setToAccount(request.getToAccount());
        record.setAmount(request.getAmount());
        record.setOrderId(request.getOrderId());
        record.setTransactionType("PAYMENT");
        record.setDescription(request.getDescription());

        try {
            // Validate amount
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid amount");
            }

            // Get accounts with lock
            Account fromAccount = accountRepository.findByAccountNumberWithLock(request.getFromAccount())
                    .orElseThrow(() -> new RuntimeException("Customer account not found: " + request.getFromAccount()));

            Account toAccount = accountRepository.findByAccountNumberWithLock(request.getToAccount())
                    .orElseThrow(() -> new RuntimeException("Store account not found: " + request.getToAccount()));

            // Check balance
            if (!fromAccount.hasSufficientBalance(request.getAmount())) {
                throw new RuntimeException("Insufficient balance");
            }

            // Perform transfer
            fromAccount.debit(request.getAmount());
            toAccount.credit(request.getAmount());

            // Save accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Update transaction record
            record.setStatus("SUCCESS");
            record.setCompletedAt(LocalDateTime.now());
            transactionRecordRepository.save(record);

            logger.info("Payment {} completed successfully for order {}", transactionId, request.getOrderId());

            return new TransferResponse(
                    transactionId,
                    "SUCCESS",
                    "Payment processed successfully",
                    fromAccount.getBalance()
            );

        } catch (Exception e) {
            logger.error("Payment {} failed for order {}: {}", transactionId, request.getOrderId(), e.getMessage());

            // Update transaction record
            record.setStatus("FAILED");
            record.setFailureReason(e.getMessage());
            record.setCompletedAt(LocalDateTime.now());
            transactionRecordRepository.save(record);

            return new TransferResponse(
                    transactionId,
                    "FAILED",
                    e.getMessage()
            );
        }
    }

    /**
     * Process refund from store to customer
     */
    @Transactional
    public TransferResponse processRefund(TransferRequest request) {
        String transactionId = UUID.randomUUID().toString();
        logger.info("Processing refund {} for order {}: {} -> {} amount: {}",
                transactionId, request.getOrderId(), request.getFromAccount(),
                request.getToAccount(), request.getAmount());

        // Create transaction record
        TransactionRecord record = new TransactionRecord();
        record.setTransactionId(transactionId);
        record.setFromAccount(request.getFromAccount());
        record.setToAccount(request.getToAccount());
        record.setAmount(request.getAmount());
        record.setOrderId(request.getOrderId());
        record.setTransactionType("REFUND");
        record.setDescription(request.getDescription());

        try {
            // Validate amount
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid refund amount");
            }

            // Get accounts with lock
            Account storeAccount = accountRepository.findByAccountNumberWithLock(request.getFromAccount())
                    .orElseThrow(() -> new RuntimeException("Store account not found: " + request.getFromAccount()));

            Account customerAccount = accountRepository.findByAccountNumberWithLock(request.getToAccount())
                    .orElseThrow(() -> new RuntimeException("Customer account not found: " + request.getToAccount()));

            // Check store balance
            if (!storeAccount.hasSufficientBalance(request.getAmount())) {
                throw new RuntimeException("Insufficient balance for refund");
            }

            // Perform refund
            storeAccount.debit(request.getAmount());
            customerAccount.credit(request.getAmount());

            // Save accounts
            accountRepository.save(storeAccount);
            accountRepository.save(customerAccount);

            // Update transaction record
            record.setStatus("SUCCESS");
            record.setCompletedAt(LocalDateTime.now());
            transactionRecordRepository.save(record);

            logger.info("Refund {} completed successfully for order {}", transactionId, request.getOrderId());

            return new TransferResponse(
                    transactionId,
                    "SUCCESS",
                    "Refund processed successfully",
                    customerAccount.getBalance()
            );

        } catch (Exception e) {
            logger.error("Refund {} failed for order {}: {}", transactionId, request.getOrderId(), e.getMessage());

            // Update transaction record
            record.setStatus("FAILED");
            record.setFailureReason(e.getMessage());
            record.setCompletedAt(LocalDateTime.now());
            transactionRecordRepository.save(record);

            return new TransferResponse(
                    transactionId,
                    "FAILED",
                    e.getMessage()
            );
        }
    }

    /**
     * Get account balance
     */
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return account.getBalance();
    }

    /**
     * Create new account
     */
    @Transactional
    public Account createAccount(String accountNumber, String accountHolder,
                                 BigDecimal initialBalance, String accountType) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new RuntimeException("Account already exists: " + accountNumber);
        }

        Account account = new Account(accountNumber, accountHolder, initialBalance, accountType);
        return accountRepository.save(account);
    }
}