UPDATE obc.tbl_obc_config SET config_value = '{"txMaxAmt": 1000.0, "txMinAmt": 1.0, "dailyLimit": 1000.0}' WHERE config_key = 'TRANSACTION_CONFIG_USD';
UPDATE obc.tbl_obc_config SET config_value = '{"txMaxAmt": 4000000.0, "txMinAmt": 100.0, "dailyLimit": 4000000.0}' WHERE config_key = 'TRANSACTION_CONFIG_KHR';
