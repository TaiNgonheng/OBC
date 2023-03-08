-- obc.tbl_obc_transaction definition

CREATE TABLE IF NOT EXISTS `tbl_obc_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `init_ref_number` varchar(20) NOT NULL,
  `trx_amount` decimal NOT NULL,
  `currency` varchar(10),
  `from_account` varchar(20) NOT NULL,
  `to_account` varchar(20) NOT NULL,
  `otp_id` varchar(10),
  `trx_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;