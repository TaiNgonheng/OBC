package com.rhbgroup.dte.obc.domains.transaction.repository;

import com.rhbgroup.dte.obc.domains.transaction.repository.entity.TransactionHistoryEntity;
import com.rhbgroup.dte.obc.domains.transaction.repository.query.TransactionHistoryQueries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionHistoryRepository
    extends PagingAndSortingRepository<TransactionHistoryEntity, Long> {

  @Query(
      name = TransactionHistoryQueries.QUERY_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER,
      countQuery = TransactionHistoryQueries.COUNT_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER,
      nativeQuery = true)
  Page<TransactionHistoryEntity> queryByFromAccount(
      @Param("accNumber") String fromAccount, Pageable pageable);

  Long deleteByFromAccountAndNewToday(String fromAccount, Integer isNewToday);
}
