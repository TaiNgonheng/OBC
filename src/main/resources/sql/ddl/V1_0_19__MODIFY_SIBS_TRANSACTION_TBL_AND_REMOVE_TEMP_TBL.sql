DROP TABLE IF EXISTS `tbl_obc_temp_transaction_history`;

ALTER TABLE `tbl_obc_sibs_transaction_history`
    MODIFY `user_id` VARCHAR(50) NOT NULL,
    MODIFY `trx_amount` DECIMAL(17, 2) NOT NULL;