
package com.banking.banking_transaction_system.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.banking.banking_transaction_system.model.Customer;
import com.banking.banking_transaction_system.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {

        try {
            return customerRepository.save(customer);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException(
                    "Customer with this email already exists"
            );
        }
    }
}
