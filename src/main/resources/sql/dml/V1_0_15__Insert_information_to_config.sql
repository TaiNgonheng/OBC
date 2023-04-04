INSERT INTO obc.tbl_obc_config
(config_key, config_value, created_date, updated_date, updated_by)
VALUES
    ('SFTP_CONFIG', JSON_OBJECT('username', 'testuser', 'password', 'testpassword', 'remoteHost', '127.0.0.1', 'remotePort', '2022', 'remotePath', 'data'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
    ('SIBS_SYNC_DATE_CONFIG', JSON_OBJECT('useSIBSSyncDate', 'true', 'sibsSyncDate', '2023-03-20'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM');