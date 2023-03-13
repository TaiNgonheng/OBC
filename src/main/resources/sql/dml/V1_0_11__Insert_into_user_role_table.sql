INSERT INTO obc.tbl_obc_user_role
(user_id, role, permissions, created_date, updated_date, updated_by)
VALUES
(1, 'APP_USER', 'can_get_balance,can_top_up', CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM'),
(2, 'SYSTEM_USER', 'can_exchange_user', CURRENT_TIMESTAMP, NULL, 'OBC_SYSTEM');