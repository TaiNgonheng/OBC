package com.rhbgroup.dte.obc.domains.transactions.repository;

import com.rhbgroup.dte.obc.domains.transactions.repository.entity.TemporaryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryTransactionRepository extends JpaRepository<TemporaryTransaction, Long> {}
