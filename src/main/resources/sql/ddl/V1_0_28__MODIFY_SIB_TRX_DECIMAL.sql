ALTER TABLE `tbl_obc_sibs_transaction_history`
    MODIFY COLUMN `amount` decimal(17, 2)  NOT NULL,
    MODIFY COLUMN `trx_amnt` decimal(17, 2)  NOT NULL,
    MODIFY COLUMN `tran_fee_amnt` decimal(17, 2)  NOT NULL,
    MODIFY COLUMN `fee_amnt_in_acct_currency` decimal(17, 2)  NOT NULL;