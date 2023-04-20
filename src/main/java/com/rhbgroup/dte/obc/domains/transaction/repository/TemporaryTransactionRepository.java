package com.rhbgroup.dte.obc.domains.transaction.repository;

import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TemporaryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryTransactionRepository extends JpaRepository<TemporaryTransaction, Long> {}
