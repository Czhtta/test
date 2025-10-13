package com.comp5348.banktransaction.repository;

import com.comp5348.banktransaction.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access Object for transaction_record database table.
 */
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
}
