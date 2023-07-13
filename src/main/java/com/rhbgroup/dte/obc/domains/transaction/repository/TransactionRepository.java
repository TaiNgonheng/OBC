package com.rhbgroup.dte.obc.domains.transaction.repository;

import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionEntity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

  Optional<TransactionEntity> findByInitRefNumber(String initRefNumber);

  @Query(
      value =
          "select \n"
              + "  (SUM(trx_amount) + SUM(trx_fee)) AS total \n"
              + "from \n"
              + "  tbl_obc_transaction \n"
              + "where \n"
              + "  from_account = :fromAccount \n"
              + "  and trx_status = :trxStatus \n"
              + "  and CAST(trx_date AS date) = :trxDate \n"
              + "  and user_id = :userId\n",
      nativeQuery = true)
  Double sumTodayTotalDebitAmountByAcctId(
      String fromAccount, String trxStatus, LocalDate trxDate, Long userId);
}
