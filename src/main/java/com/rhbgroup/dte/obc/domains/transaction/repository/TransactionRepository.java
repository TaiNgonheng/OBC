package com.rhbgroup.dte.obc.domains.transaction.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

  String QUERY_1 = "SELECT * FROM tbl_obc_transaction WHERE user_id = ?1";

  Optional<TransactionEntity> findByInitRefNumber(String initRefNumber);

  @Query(value = QUERY_1, nativeQuery = true)
  List<TransactionEntity> createNativeQuery(Long userId);
}
