-- obc.tbl_obc_account definition

CREATE TABLE IF NOT EXISTS `tbl_obc_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bakong_id` varchar(50) NOT NULL,
  `account_id` varchar(50) NOT NULL,
  `user_id` bigint NOT NULL,
  `active` bit(1) NOT NULL DEFAULT 1,
  `created_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_date` timestamp DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;