-- obc.tbl_obc_user_role definition

CREATE TABLE IF NOT EXISTS `tbl_obc_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(20) DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `permissions` varchar(50) NOT NULL,
  `created_date` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_date` timestamp DEFAULT NULL,
  `updated_by` varchar(20) DEFAULT 'OBC_SYS' NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;