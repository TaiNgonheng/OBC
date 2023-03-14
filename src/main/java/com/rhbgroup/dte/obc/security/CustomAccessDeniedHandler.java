package com.rhbgroup.dte.obc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.ResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {

    ResponseWrapper responseWrapper =
        new ResponseWrapper()
            .status(
                new ResponseStatus()
                    .errorCode(ResponseMessage.AUTHENTICATION_FAILED.getCode().toString())
                    .errorMessage(ResponseMessage.AUTHENTICATION_FAILED.getMsg())
                    .code(AppConstants.STATUS.ERROR));

    response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
    response.setContentType("application/json");
    response.getWriter().write(new ObjectMapper().writeValueAsString(responseWrapper));
  }
}
