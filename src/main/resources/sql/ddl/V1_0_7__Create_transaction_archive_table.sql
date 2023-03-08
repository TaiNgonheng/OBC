-- obc.tbl_obc_transaction_archive definition

CREATE TABLE IF NOT EXISTS `tbl_obc_transaction_archive` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trx_ref_number` varchar(20) NOT NULL,
  `trx_amount` decimal NOT NULL,
  `currency` varchar(10),
  `from_account` varchar(20) NOT NULL,
  `to_account` varchar(20) NOT NULL,
  `trx_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `allow_restore` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;