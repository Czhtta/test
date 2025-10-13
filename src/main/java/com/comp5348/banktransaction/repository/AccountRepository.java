package com.comp5348.banktransaction.repository;

import com.comp5348.banktransaction.model.Account;
import com.comp5348.banktransaction.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data Access Object for account database table.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Spring Data JPA will automatically generate the implementation.
    // Find more about it at https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    Optional<Account> findByIdAndCustomer(long id, Customer customer);
}
