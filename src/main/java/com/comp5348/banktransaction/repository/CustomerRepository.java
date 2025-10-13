package com.comp5348.banktransaction.repository;

import com.comp5348.banktransaction.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access Object for customer database table.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
