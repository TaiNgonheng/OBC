-- obc.tbl_obc_transaction definition

CREATE TABLE IF NOT EXISTS `tbl_obc_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `transfer_type` varchar(20),
  `transfer_message` varchar(250) DEFAULT NULL,
  `init_ref_number` varchar(50) DEFAULT NULL,
  `trx_id` varchar(20) NOT NULL,
  `trx_amount` decimal NOT NULL,
  `trx_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `trx_completion_date` timestamp DEFAULT NULL,
  `trx_hash` timestamp NOT NULL,
  `trx_status` varchar(20) DEFAULT NULL,
  `from_account` varchar(20) NOT NULL,
  `from_account_currency` varchar(10) DEFAULT NULL,
  `payer_name` varchar(50) DEFAULT NULL,
  `to_account` varchar(20) NOT NULL,
  `to_account_currency` varchar(10) DEFAULT NULL,
  `recipient_bic` varchar(50) DEFAULT NULL,
  `recipient_name` varchar(50) DEFAULT NULL,
  `otp_id` varchar(10),
  `credit_debit_indicator` varchar(5) DEFAULT "D" NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE obc.tbl_obc_transaction ADD CONSTRAINT tbl_obc_transaction_FK FOREIGN KEY (user_id) REFERENCES obc.tbl_obc_profile(id) ON DELETE CASCADE;
