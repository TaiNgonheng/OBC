package com.rhbgroup.dte.obc.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class Sha256PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // TODO customize SHA256 password encoder
        return "";
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // TODO
        return false;
    }
}
