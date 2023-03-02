package com.rhbgroup.dte.obc.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenUtils {

  @Value("${app.jwt-secret}")
  private String mySecret;

  @Value("${app.jwt-ttl}")
  private Long tokenTTL; // in second
  @Autowired
  private PasswordEncoder encoder;

  public String getUsernameFromJwtToken(String jwt) {
    return getClaimFromToken(jwt, Claims::getSubject);
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    try {
      Claims claims = Jwts.parser().setSigningKey(mySecret).parseClaimsJws(token).getBody();
      return claimsResolver.apply(claims);

    } catch (ExpiredJwtException ex) {
      throw new UserAuthenticationException(ResponseMessage.SESSION_EXPIRED);

    } catch (Exception ex) {
      throw new UserAuthenticationException(ResponseMessage.INVALID_TOKEN);
    }
  }

  public String generateJwt(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    try {
      return Jwts.builder()
          .setSubject(userPrincipal.getUsername())
          .setPayload(new ObjectMapper().writeValueAsString(userPrincipal))
          .setIssuedAt(new Date())
          .setExpiration(new Date((new Date()).getTime() + (tokenTTL * 1000)))
          .signWith(SignatureAlgorithm.HS512, mySecret)
          .compact();

    } catch (JsonProcessingException ex) {
      return null;
    }
  }

  public String encodePassword(String password) {
    return encoder.encode(password);
  }

  public boolean isExpired(String token) {
    Date expiryDate = getClaimFromToken(token, Claims::getExpiration);
    return expiryDate.before(new Date());
  }

}
