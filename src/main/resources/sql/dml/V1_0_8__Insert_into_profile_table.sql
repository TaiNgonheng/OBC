INSERT INTO obc.tbl_obc_profile
(id, username, password, otp_id, otp_verified_status, otp_verified_date, mobile_no, email, cif_no, status, created_date, updated_date, updated_by)
VALUES
(1, 'admin', '$2a$10$EKCP51nvNWW/x3NUre9rZOk7SB7sWW/zLX5W/XTwfpu0c5RhbNnR.', '123456', b'0', NULL, '955987654321', 'admin@gmail.com', '', 'UNLINKED', CURRENT_TIMESTAMP, NULL, ''),
(2, 'gowave_user', '$2a$10$kmk7gTewIcKdsdAmhRm4NO4U7uvqJVUJU6lWsAYLgufW7i6HrEDce', '123456', b'0', NULL, '955123456789', 'gowave+user@gmail.com', '', 'UNLINKED', CURRENT_TIMESTAMP, NULL, '');