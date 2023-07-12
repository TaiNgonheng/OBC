package com.rhbgroup.dte.obc.domains.transaction.repository;

import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

  Optional<TransactionEntity> findByInitRefNumber(String initRefNumber);

  @Query(
      value =
          "select * from tbl_obc_transaction where from_account = :fromAccount and trx_status = :trxStatus and CAST( trx_date AS date ) = :trxDate",
      nativeQuery = true)
  List<TransactionEntity> findByFromAccountAndTrxStatusAndTrxDate(
      String fromAccount, String trxStatus, LocalDate trxDate);
}
