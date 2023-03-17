INSERT INTO obc.tbl_obc_config
(config_key, config_value, created_date, updated_date, updated_by)
VALUES
('CDRB_ACCOUNT', JSON_OBJECT('username', 'brodyboiloy', 'encPwd1', '$2a$10$5hj62dCe7TLqHXCimpoNNu327KddhKyPQ88dFXuvHr4BVxbsfHoe6', 'encPwd2', '5hj62dCe7TLqHXCimpoNNu327KddhKyPQ88dFXuvHr4BVxbsfHoe6'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('PG1_ACCOUNT', JSON_OBJECT('username', 'soap_user', 'password', '+66xZ_feBcRpYP9e'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('PG1_URL', JSON_OBJECT('value', 'http://10.202.38.27/'), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('REQUIRED_INIT_ACCOUNT_OTP', JSON_OBJECT('value', 1), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
('TRANSACTION_CONFIG', JSON_OBJECT('txMinAmt', 10, 'txMaxAmt', 10.000, 'defaultCcy', 'USD', 'requiredTrxOtp', 0), CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM');