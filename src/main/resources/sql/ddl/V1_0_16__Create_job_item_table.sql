CREATE TABLE IF NOT EXISTS `tbl_job_item` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `job_name` VARCHAR(50) NOT NULL,
    `job_group` VARCHAR(50) NOT NULL,
    `status` boolean NOT NULL,
    `trigger_at` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=UTF8