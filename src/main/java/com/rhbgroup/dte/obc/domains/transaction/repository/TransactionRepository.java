package com.rhbgroup.dte.obc.domains.transaction.repository;

import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

  Optional<TransactionEntity> findByInitRefNumber(String initRefNumber);
}
