package com.rhbgroup.dte.obc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenUtils {

    @Value("${app.jwt-secret}")
    private String mySecret;

    @Autowired
    private PasswordEncoder encoder;

    @SneakyThrows
    public String getUsernameFromJwtToken(String jwt) {
        Claims body = Jwts.parser().setSigningKey(mySecret).parseClaimsJws(jwt).getBody();
        return body == null ? "" : body.getSubject();
    }

    @SneakyThrows
    public String generateJwt(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + (5 * 1000 * 60)))
                .signWith(SignatureAlgorithm.HS512, mySecret)
                .compact();
    }

    @SneakyThrows
    public String encodePassword(String password) {
        return encoder.encode(password);
    }

}
