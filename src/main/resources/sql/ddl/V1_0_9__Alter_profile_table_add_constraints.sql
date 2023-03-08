ALTER TABLE obc.tbl_obc_profile
ADD CONSTRAINT tbl_obc_profile_UN
UNIQUE KEY (username,mobile_no,cif_no);
