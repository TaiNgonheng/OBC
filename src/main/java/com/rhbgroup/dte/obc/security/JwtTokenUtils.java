package com.rhbgroup.dte.obc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenUtils {

  @Value("${app.jwt-secret}")
  private String mySecret;

  @Value("${app.jwt-ttl}")
  private Long tokenTTL; // in second

  @Autowired private PasswordEncoder encoder;

  public String getUsernameFromJwtToken(String jwt) {
    try {
      return getClaimFromToken(jwt, Claims::getSubject);
    } catch (Exception ex) {
      return null;
    }
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    Claims claims = Jwts.parser().setSigningKey(mySecret).parseClaimsJws(token).getBody();
    return claimsResolver.apply(claims);
  }

  public boolean notValidFormat(String token) {
    try {
      return StringUtils.isBlank(getClaimFromToken(token, Claims::getSubject));

    } catch (MalformedJwtException ex) {
      return true;

    } catch (Exception ex) {
      return false;
    }
  }

  public String generateJwt(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    return Jwts.builder()
        .setSubject(userPrincipal.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + (tokenTTL * 1000)))
        .signWith(SignatureAlgorithm.HS512, mySecret)
        .compact();
  }

  public String encodePassword(String password) {
    return encoder.encode(password);
  }

  public boolean isExpired(String token) {
    try {
      Date expiryDate = getClaimFromToken(token, Claims::getExpiration);
      return expiryDate.before(new Date());

    } catch (Exception ex) {
      return true;
    }
  }
}
