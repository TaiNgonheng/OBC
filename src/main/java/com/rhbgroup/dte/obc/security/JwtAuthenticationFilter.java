package com.rhbgroup.dte.obc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.ResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenManager jwtTokenManager;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    AuthenticationStatus authResponse = jwtTokenManager.verifyRequest(request);
    switch (authResponse.getResult()) {
      case SUCCESS:
        jwtTokenManager.supplySecurityContext(request, authResponse.getJwt());
        filterChain.doFilter(request, response);
        break;
      case EXPIRED:
        captureError(response, ResponseMessage.SESSION_EXPIRED);
        break;
      case INVALID:
        captureError(response, ResponseMessage.INVALID_TOKEN);
        break;
      default:
        filterChain.doFilter(request, response);
        break;
    }
  }

  private void captureError(HttpServletResponse response, ResponseMessage message) {
    try {
      ResponseWrapper responseWrapper =
          new ResponseWrapper()
              .status(
                  new ResponseStatus()
                      .errorCode(message.getCode().toString())
                      .errorMessage(message.getMsg())
                      .code(AppConstants.STATUS.ERROR));

      response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
      response.setContentType("application/json");
      response.getWriter().write(objectMapper.writeValueAsString(responseWrapper));

    } catch (IOException ex) {
      log.error("processError::HTTP >> {}" + ex.getMessage());
    }
  }
}
