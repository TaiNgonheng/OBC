package com.rhbgroup.dte.obc.domains.transaction.repository.query;

public class TransactionHistoryQueries {

  private TransactionHistoryQueries() {}

  public static final String QUERY_TRANSACTION_HISTORY_BY_ACCOUNT_NUMBER =
      "SELECT sth.id,\n"
          + "       sth.transfer_type,\n"
          + "       sth.transfer_message,\n"
          + "       sth.trx_id,\n"
          + "       sth.trx_amount_in_acct_currency,\n"
          + "       sth.trx_date,\n"
          + "       sth.trx_hash,\n"
          + "       sth.trx_status,\n"
          + "       sth.currency_code,\n"
          + "       sth.from_account,\n"
          + "       sth.to_account,\n"
          + "       sth.credit_debit_indicator,\n"
          + "       sth.channel_id,\n"
          + "       sth.trx_amnt,\n"
          + "       sth.tran_curr,\n"
          + "       sth.tran_fee_amnt,\n"
          + "       sth.fee_amnt_in_acct_currency\n"
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
