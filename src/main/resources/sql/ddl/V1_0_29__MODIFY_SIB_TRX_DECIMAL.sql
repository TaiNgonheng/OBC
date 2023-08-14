ALTER TABLE `tbl_obc_sibs_transaction_history`
    MODIFY COLUMN `fee_amnt_in_acct_currency` decimal(17, 2)  NOT NULL;

ALTER TABLE obc.tbl_obc_sibs_transaction_history CHANGE `fee_amnt_in_acct_currency` `fee_amnt` decimal NOT NULL;