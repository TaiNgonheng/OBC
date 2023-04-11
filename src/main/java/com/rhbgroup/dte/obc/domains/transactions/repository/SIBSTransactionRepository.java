package com.rhbgroup.dte.obc.domains.transactions.repository;

import com.rhbgroup.dte.obc.domains.transactions.repository.entity.SIBSTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SIBSTransactionRepository extends JpaRepository<SIBSTransaction, Long> {}
