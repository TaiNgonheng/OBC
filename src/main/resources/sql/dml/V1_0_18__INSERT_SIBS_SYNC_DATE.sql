INSERT INTO obc.tbl_obc_config
(config_key, config_value, created_date, updated_date, updated_by)
VALUES
    ('SIBS_DATE_CONFIG', JSON_OBJECT('useSIBSSyncDate', 'true', 'sibsSyncDate', '20230320'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM');