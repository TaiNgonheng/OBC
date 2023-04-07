-- obc.tbl_obc_transaction definition

CREATE TABLE IF NOT EXISTS `tbl_obc_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `transfer_type` varchar(20),
  `transfer_message` varchar(250) DEFAULT NULL,
  `init_ref_number` varchar(32) DEFAULT NULL,
  `trx_hash` varchar(50) DEFAULT NULL,
  `trx_amount` decimal NOT NULL,
  `trx_fee` decimal NOT NULL,
  `trx_cashback` decimal NOT NULL,
  `trx_ccy` varchar(5) NOT NULL,
  `trx_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `trx_completion_date` timestamp DEFAULT NULL,
  `trx_status` varchar(10) DEFAULT NULL,
  `from_account` varchar(50) NOT NULL,
  `payer_name` varchar(30) DEFAULT NULL,
  `to_account` varchar(50) NOT NULL,
  `to_account_currency` varchar(5) NOT NULL,
  `recipient_bic` varchar(30) DEFAULT NULL,
  `recipient_name` varchar(30) DEFAULT NULL,
  `credit_debit_indicator` varchar(5) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
