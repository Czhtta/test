package com.comp5348.banktransaction.service;

import com.comp5348.banktransaction.dto.CustomerDTO;
import com.comp5348.banktransaction.model.Customer;
import com.comp5348.banktransaction.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Business logic for customer management.
 */
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerDTO createCustomer(String firstName, String lastName) {
        Customer customer = new Customer(firstName, lastName);
        customer = customerRepository.save(customer);
        return new CustomerDTO(customer);
    }
}
