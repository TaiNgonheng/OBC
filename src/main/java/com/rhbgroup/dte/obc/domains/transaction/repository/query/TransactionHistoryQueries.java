package com.rhbgroup.dte.obc.domains.transaction.repository.query;

public class TransactionHistoryQueries {

  private TransactionHistoryQueries() {}

  public static final String QUERY_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER =
      "SELECT sth.transfer_type, sth.transfer_message, sth.trx_id, sth.trx_amount, sth.trx_completionDate, "
          + "sth.trx_hash, sth.trx_status, sth.trx_ccy, sth.from_account, sth.to_account, sth.credit_debit_indicator "
          + "FROM tbl_obc_sibs_transaction_history sth "
          + "WHERE sth.from_account = :accNumber";

  public static final String COUNT_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER =
      "SELECT COUNT(sth.id) "
          + "FROM tbl_obc_sibs_transaction_history sth"
          + "WHERE sth.from_account = :accNumber";
}
