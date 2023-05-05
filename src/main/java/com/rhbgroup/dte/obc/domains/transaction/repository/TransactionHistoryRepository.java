package com.rhbgroup.dte.obc.domains.transaction.repository;

import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionHistoryEntity;
import com.rhbgroup.dte.obc.domains.transaction.repository.query.TransactionHistoryQueries;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionHistoryRepository
    extends PagingAndSortingRepository<TransactionHistoryEntity, Long> {

  @Query(
      value = TransactionHistoryQueries.QUERY_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER,
      countQuery = TransactionHistoryQueries.COUNT_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER,
      nativeQuery = true)
  Page<TransactionHistoryEntity> queryByFromAccount(
      @Param("accNumber") String fromAccount, Pageable pageable);

  @Modifying
  @Query(value = TransactionHistoryQueries.DELETE_TODAY_RECORD, nativeQuery = true)
  Integer deleteTodayTransactionByAccountNumber(@Param("fromAccount") String fromAccount);

  @Transactional
  @Modifying
  @Query(value = TransactionHistoryQueries.DELETE_RECORDS_BY_DATE, nativeQuery = true)
  void deleteAllByTrxDate(LocalDate date);
}
