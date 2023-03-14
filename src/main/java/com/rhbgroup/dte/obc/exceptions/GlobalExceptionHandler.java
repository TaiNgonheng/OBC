package com.rhbgroup.dte.obc.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.model.ResponseStatus;
import com.rhbgroup.dte.obc.model.ResponseWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ResponseWrapper> jpaException(DataIntegrityViolationException ex) {

    try {
      JsonNode constraintViolationException =
          new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(ex.getCause()));

      JsonNode cause = constraintViolationException.get("cause");
      String message = cause.get("message").asText();

      ResponseWrapper errorResponse =
          new ResponseWrapper()
              .status(
                  new ResponseStatus()
                      .code(AppConstants.STATUS.ERROR)
                      .errorCode(ResponseMessage.CONSTRAINT_VIOLATION_ERROR.getCode().toString())
                      .errorMessage(message));

      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    } catch (Exception exception) {

      ResponseWrapper errorResponse =
          new ResponseWrapper()
              .status(
                  new ResponseStatus()
                      .code(AppConstants.STATUS.ERROR)
                      .errorCode(ResponseMessage.CONSTRAINT_VIOLATION_ERROR.getCode().toString())
                      .errorMessage(exception.getMessage()));
      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
  }

  @ExceptionHandler(UserAuthenticationException.class)
  public ResponseEntity<ResponseWrapper> userAuthenticationException(
      UserAuthenticationException ex) {

    ResponseStatus status =
        new ResponseStatus()
            .code(AppConstants.STATUS.ERROR)
            .errorCode(ex.getResponseMessage().getCode().toString())
            .errorMessage(ex.getResponseMessage().getMsg());

    return new ResponseEntity<>(new ResponseWrapper().status(status), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BizException.class)
  public ResponseEntity<ResponseWrapper> authenticationException(BizException ex) {

    ResponseStatus status =
        new ResponseStatus()
            .code(AppConstants.STATUS.ERROR)
            .errorCode(ex.getResponseMessage().getCode().toString())
            .errorMessage(ex.getResponseMessage().getMsg());

    return new ResponseEntity<>(new ResponseWrapper().status(status), HttpStatus.BAD_REQUEST);
  }
}
