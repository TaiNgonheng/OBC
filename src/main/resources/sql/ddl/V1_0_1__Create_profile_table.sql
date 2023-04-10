-- obc.tbl_obc_profile definition

CREATE TABLE IF NOT EXISTS `tbl_obc_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) UNIQUE NOT NULL,
  `password` varchar(100) NOT NULL,
  `otp_verified_status` bit(1) DEFAULT 0,
  `otp_verified_date` timestamp DEFAULT NULL,
  `mobile_no` varchar(20) UNIQUE DEFAULT NULL,
  `email` varchar(100) UNIQUE DEFAULT NULL,
  `cif_no` varchar(100) DEFAULT NULL,
  `login_attempt` tinyint DEFAULT 0,
  `lock_time` timestamp DEFAULT NULL,
  `deleted` bit(1) DEFAULT 0,
  `created_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_date` timestamp DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE obc.tbl_obc_profile ADD CONSTRAINT tbl_obc_profile_UN UNIQUE KEY (username);