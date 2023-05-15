ALTER TABLE obc.tbl_obc_transaction MODIFY COLUMN trx_amount DECIMAL(17,2) NOT NULL;
ALTER TABLE obc.tbl_obc_transaction MODIFY COLUMN trx_fee DECIMAL(17,2) NOT NULL;
ALTER TABLE obc.tbl_obc_transaction MODIFY COLUMN trx_cashback DECIMAL(17,2) NOT NULL;