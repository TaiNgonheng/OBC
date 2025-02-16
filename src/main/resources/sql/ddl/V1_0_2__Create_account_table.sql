-- obc.tbl_obc_account definition

CREATE TABLE IF NOT EXISTS `tbl_obc_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `bakong_id` varchar(50) DEFAULT NULL,
  `account_id` varchar(50) DEFAULT NULL,
  `account_name` varchar(50) DEFAULT NULL,
  `account_type` varchar(20) DEFAULT NULL,
  `account_ccy` varchar (5) DEFAULT NULL,
  `account_status` varchar(10) DEFAULT "ACTIVE" NULL,
  `linked_status` varchar(10) DEFAULT "PENDING" NULL,
  `created_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_date` timestamp DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
