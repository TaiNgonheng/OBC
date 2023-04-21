DROP TABLE IF EXISTS `tbl_obc_temp_transaction_history`;

ALTER TABLE `tbl_obc_sibs_transaction_history`
    ADD `new_today` smallint,
    MODIFY `trx_hash` varchar(100) NOT NULL;