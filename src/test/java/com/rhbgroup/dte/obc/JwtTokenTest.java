package com.rhbgroup.dte.obc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JwtTokenTest {

  @Test
  void testJwtValidity() {
    String token =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzb2FwX3VzZXIiLCJhdXRoIjoiUk9MRV9BRE1JTklTVFJBVE9SLFJPTEVfTUFOQUdFUixST0xFX1BVQkxJQ19HQVRFV0FZLFJPTEVfU09BUCIsInBlcm1pc3Npb25zIjpbXSwiaWQiOjUwMDA4LCJleHAiOjE2NzkyODUxMDJ9.fbEqCLji7zXPK8gh-lnnm09GcnsCA4eqmS1DMfjuPXGnfgOQLeNmkewtk30nGEYj88pC97dztEUc4RN2qPcn_A";
    Assertions.assertTrue(isExtTokenExpired(token));
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
