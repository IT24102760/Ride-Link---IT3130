package com.ridelink.accountservice.repository;

import com.ridelink.accountservice.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

// Database access for accounts (Spring writes the queries from the method names)
public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByEmail(String email);   // used by login

    boolean existsByEmail(String email);           // used by register (duplicate check)
}
