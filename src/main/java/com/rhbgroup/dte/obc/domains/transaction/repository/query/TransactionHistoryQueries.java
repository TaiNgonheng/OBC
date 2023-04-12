package com.rhbgroup.dte.obc.domains.transaction.repository.query;

public class TransactionHistoryQueries {

  private TransactionHistoryQueries() {}

  public static final String QUERY_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER =
      ""
          + "SELECT * "
          + "FROM tbl_obc_sibs_transaction_history "
          + "WHERE from_account = :accNumber";

  public static final String COUNT_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER =
      ""
          + "SELECT COUNT(rs.id) "
          + "FROM tbl_obc_sibs_transaction_history "
          + "WHERE from_account = :accNumber";
}
