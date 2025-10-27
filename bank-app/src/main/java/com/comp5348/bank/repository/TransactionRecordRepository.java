package com.comp5348.bank.repository;

import com.comp5348.bank.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for TransactionRecord operations
 */
@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    Optional<TransactionRecord> findByTransactionId(String transactionId);

    Optional<TransactionRecord> findByOrderId(String orderId);
}