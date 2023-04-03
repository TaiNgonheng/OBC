package com.rhbgroup.dte.obc.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.common.util.crypto.AESCryptoUtil;
import com.rhbgroup.dte.obc.common.util.crypto.CryptoUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtils {

  public static final String CLAIMS_AUTHORIZATIONS = "auth";
  public static final String CLAIMS_USER = "user";
  public static final String CLAIMS_EXPIRY_DATE = "exp";
  public static final String BEARER_PREFIX = "Bearer ";

  public static final String AES_KEY = "1234567812345678";
  public static final String AES_IV = "1234567812345678";

  @Value("${obc.security.jwt-secret}")
  protected String jwtSecrete;

  @Value("${obc.security.jwt-ttl}")
  protected Long tokenTTL; // in second

  public String getSubject(String jwt) {
    try {
      return getClaimFromToken(extractJwt(jwt), Claims::getSubject);
    } catch (Exception ex) {
      return StringUtils.EMPTY;
    }
  }

  public String getUserId(String jwt) {
    try {
      String encUserId =
          getClaimFromToken(extractJwt(jwt), claims -> claims.get(CLAIMS_USER).toString());
      return new String(
          AESCryptoUtil.decrypt(
              CryptoUtil.decodeHex(encUserId), AES_KEY, AES_IV.getBytes(StandardCharsets.UTF_8)),
          StandardCharsets.UTF_8);
    } catch (Exception ex) {
      return StringUtils.EMPTY;
    }
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    Claims claims = Jwts.parser().setSigningKey(jwtSecrete).parseClaimsJws(token).getBody();
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

  public String generateJwtAppUser(Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    Claims claims = Jwts.claims().setSubject(userDetails.getBakongId());
    if (StringUtils.isNotBlank(userDetails.getPermissions())) {
      claims.put(CLAIMS_AUTHORIZATIONS, userDetails.getPermissions().split(","));
    }
    claims.put(CLAIMS_EXPIRY_DATE, Instant.now().toEpochMilli() + (tokenTTL * 100));
    claims.put(
        CLAIMS_USER,
        CryptoUtil.encodeHexString(
            AESCryptoUtil.encrypt(
                userDetails.getUserId().toString(),
                AES_KEY,
                AES_IV.getBytes(StandardCharsets.UTF_8))));

    return Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS512, jwtSecrete)
        .compact();
  }

  public String generateJwt(Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
    if (StringUtils.isNotBlank(userDetails.getPermissions())) {
      claims.put(CLAIMS_AUTHORIZATIONS, userDetails.getPermissions().split(","));
    }

    return Jwts.builder()
        .setClaims(claims)
        .setExpiration(new Date((new Date()).getTime() + (tokenTTL * 1000)))
        .signWith(SignatureAlgorithm.HS512, jwtSecrete)
        .compact();
  }

  public String extractJwt(String jwt) {
    if (StringUtils.isBlank(jwt)) return StringUtils.EMPTY;
    return jwt.contains(BEARER_PREFIX) ? jwt.substring(BEARER_PREFIX.length()) : jwt;
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
      long exp = jsonNode.get(CLAIMS_EXPIRY_DATE).longValue();
      Date expDate = new Date(exp * 1000);
      return expDate.before(new Date());

    } catch (Exception ex) {
      return true;
    }
  }
}
