INSERT INTO obc.tbl_obc_config
(config_key, config_value, created_date, updated_date, updated_by)
VALUES
('CDRB_ACCOUNT', JSON_OBJECT('username', 'brodyboiloy', 'encPwd1', '$2a$10$5hj62dCe7TLqHXCimpoNNu327KddhKyPQ88dFXuvHr4BVxbsfHoe6', 'encPwd2', '5hj62dCe7TLqHXCimpoNNu327KddhKyPQ88dFXuvHr4BVxbsfHoe6'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('PG1_ACCOUNT', JSON_OBJECT('username', 'soap_user', 'password', '+66xZ_feBcRpYP9e'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('PG1_URL', JSON_OBJECT('value', 'http://10.202.38.27/'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('REQUIRED_INIT_ACCOUNT_OTP', JSON_OBJECT('value', 1), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('TRANSACTION_CONFIG', JSON_OBJECT('txMinAmt', 10, 'txMaxAmt', 10.000, 'defaultCcy', 'USD', 'requiredTrxOtp', 0), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('INFO_BIP_ACCOUNT', JSON_OBJECT('client_id', 'test_client_id', 'client_secret', 'test_client_secret', 'grant_type', 'client_credentials'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('INFO_BIP_APP_ID', JSON_OBJECT('value', 'BBE417D8EE1A91D0F5EA415619E549B8'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('INFO_BIP_OTP_MESSAGE_ID', JSON_OBJECT('value', '4BEE2103E2795594DFB6B4FD6B328CF8'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('INFO_BIP_URL', JSON_OBJECT('value', 'https://r5xqjm.api.infobip.com'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM');
