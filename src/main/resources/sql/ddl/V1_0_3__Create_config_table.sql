-- obc.tbl_obc_config definition

CREATE TABLE IF NOT EXISTS `tbl_obc_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_name` varchar(50) NOT NULL,
  `service_key` varchar(50) NOT NULL,
  `secret` varchar(100) NOT NULL,
  `token` varchar(250) NOT NULL,
  `required_trx_otp` bit(1) NOT NULL DEFAULT 0,
  `transaction_config` longtext,
  `created_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_date` timestamp DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;