package com.rhbgroup.dte.obc.exceptions;

import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.ResponseWrapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleErrorException(IllegalArgumentException ex) {

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("message", ex.getMessage());
    body.put("code", "999999");

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UserAuthenticationException.class)
  public ResponseEntity<ResponseWrapper> authenticationException(UserAuthenticationException ex) {

    ResponseStatus status = new ResponseStatus();
    status.setCode(1);
    status.setErrorCode(ex.getResponseMessage().getCode().toString());
    status.setErrorMessage(ex.getResponseMessage().getMsg());

    ResponseWrapper response = new ResponseWrapper();
    response.setStatus(status);
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }
}
