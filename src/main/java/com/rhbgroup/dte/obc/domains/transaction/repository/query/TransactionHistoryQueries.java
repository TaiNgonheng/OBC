package com.rhbgroup.dte.obc.domains.transaction.repository.query;

public class TransactionHistoryQueries {

  private TransactionHistoryQueries() {}

  public static final String QUERY_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER =
      "SELECT sth.id, sth.transfer_type, sth.transfer_message, sth.trx_id, sth.trx_amnt, sth.trx_date, "
          + "sth.trx_hash, sth.trx_status, sth.tranCurr, sth.from_account, sth.to_account, sth.credit_debit_indicator, sth.channel_id "
          + "FROM tbl_obc_sibs_transaction_history sth "
          + "WHERE sth.from_account = :accNumber";

  public static final String COUNT_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER =
      "SELECT COUNT(sth.id) "
          + "FROM tbl_obc_sibs_transaction_history sth "
          + "WHERE sth.from_account = :accNumber";

  public static final String DELETE_TODAY_RECORD =
      "DELETE FROM tbl_obc_sibs_transaction_history WHERE from_account = :fromAccount AND DATE(trx_date) = CURDATE()";

  public static final String DELETE_RECORDS_BY_DATE =
      "DELETE FROM obc.tbl_obc_sibs_transaction_history WHERE date(trx_date) = date(?1)";

  public static final String DELETE_SIBS_TODAY_RECORD_BY_ACCOUNT_NUMBER =
      "DELETE FROM obc.tbl_obc_sibs_transaction_history WHERE from_account = :fromAccount AND DATE(trx_date) = DATE(:sibsSyncDate)";
}
