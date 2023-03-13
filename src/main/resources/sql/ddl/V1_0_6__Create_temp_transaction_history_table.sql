-- obc.tbl_obc_temp_transaction_history definition

CREATE TABLE IF NOT EXISTS `tbl_obc_temp_transaction_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `transfer_type` varchar(20),
  `transfer_message` varchar(250) DEFAULT NULL,
  `trx_id` varchar(20) NOT NULL,
  `trx_amount` decimal NOT NULL,
  `trx_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `trx_completion_date` timestamp DEFAULT NULL,
  `trx_hash` timestamp NOT NULL,
  `trx_status` varchar(20) DEFAULT NULL,
  `trx_ccy` varchar(10) DEFAULT NULL,
  `from_account` varchar(20) NOT NULL,
  `to_account` varchar(20) NOT NULL,
  `credit_debit_indicator` varchar(5) DEFAULT "D" NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE obc.tbl_obc_temp_transaction_history ADD CONSTRAINT tbl_obc_temp_transaction_history_FK FOREIGN KEY (user_id) REFERENCES obc.tbl_obc_profile(id) ON DELETE CASCADE;