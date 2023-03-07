-- obc.tbl_obc_profile definition

CREATE TABLE IF NOT EXISTS `tbl_obc_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `otp_id` varchar(50) DEFAULT NULL,
  `otp_verify_status` tinyint DEFAULT 0,
  `mobile_no` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `cif_no` varchar(100) DEFAULT NULL,
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;