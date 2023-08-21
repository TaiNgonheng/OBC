ALTER TABLE
    tbl_obc_account
    ADD COLUMN
        otp_verified bit(1) DEFAULT 0,
    ADD COLUMN
        otp_verified_date_time timestamp DEFAULT NULL;