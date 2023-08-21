ALTER TABLE obc.tbl_obc_sibs_transaction_history ADD `trx_amnt` decimal NOT NULL;
ALTER TABLE obc.tbl_obc_sibs_transaction_history ADD `tranCurr` varchar(10)  NOT NULL;
ALTER TABLE obc.tbl_obc_sibs_transaction_history ADD `tran_fee_amnt` decimal NOT NULL;
ALTER TABLE obc.tbl_obc_sibs_transaction_history ADD `fee_amnt_in_acct_currency` decimal NOT NULL;