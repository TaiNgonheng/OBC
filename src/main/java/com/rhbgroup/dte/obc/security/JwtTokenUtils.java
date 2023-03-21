package com.rhbgroup.dte.obc.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtils {

  @Value("${app.jwt-secret}")
  String mySecret;

  @Value("${app.jwt-ttl}")
  Long tokenTTL; // in second

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

  public String extractJwt(String jwt){
    return StringUtils.isNotBlank(jwt) && jwt.contains("Bearer") ? jwt.substring(7): new String("");
  }

  public boolean isExpired(String token) {
    try {
      Date expiryDate = getClaimFromToken(token, Claims::getExpiration);
      return expiryDate.before(new Date());

    } catch (Exception ex) {
      return true;
    }
  }

  public boolean isExtTokenExpired(String extToken) {
    String[] tokenParts = extToken.split("\\.");
    String payload = tokenParts[1];
    ObjectMapper mapper = new ObjectMapper();
    try {
      String decodePayload = new String(Base64.getDecoder().decode(payload));
      JsonNode jsonNode = mapper.readTree(decodePayload);
      long exp = jsonNode.get("exp").longValue();
      Date expDate = new Date(exp * 1000);
      return expDate.before(new Date());

    } catch (Exception ex) {
      return true;
    }
  }
}
