CREATE TABLE IF NOT EXISTS `tbl_batch_reports` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `date` date NOT NULL,
    `status` enum('PENDING', 'COMPLETED', 'FAILED') default 'PENDING',
    `error_msg` varchar(20000),
    PRIMARY KEY (id),
    constraint date unique(date)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8