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
import org.springframework.web.bind.MethodArgumentNotValidException;
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
                      .code(AppConstants.Status.ERROR)
                      .errorCode(ResponseMessage.CONSTRAINT_VIOLATION_ERROR.getCode().toString())
                      .errorMessage(message));

      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    } catch (Exception exception) {

      ResponseWrapper errorResponse =
          new ResponseWrapper()
              .status(
                  new ResponseStatus()
                      .code(AppConstants.Status.ERROR)
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
            .code(AppConstants.Status.ERROR)
            .errorCode(ex.getResponseMessage().getCode().toString())
            .errorMessage(ex.getResponseMessage().getMsg());

    return new ResponseEntity<>(new ResponseWrapper().status(status), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BizException.class)
  public ResponseEntity<ResponseWrapper> bizException(BizException ex) {

    ResponseStatus status =
        new ResponseStatus()
            .code(AppConstants.Status.ERROR)
            .errorCode(ex.getResponseMessage().getCode().toString())
            .errorMessage(ex.getResponseMessage().getMsg());

    return new ResponseEntity<>(
        new ResponseWrapper().status(status).data(null), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InternalException.class)
  public ResponseEntity<ResponseWrapper> internalException(InternalException ex) {

    ResponseStatus status =
        new ResponseStatus()
            .code(AppConstants.Status.ERROR)
            .errorCode(ex.getResponseMessage().getCode().toString())
            .errorMessage(ex.getResponseMessage().getMsg());

    return new ResponseEntity<>(
        new ResponseWrapper().status(status), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseWrapper> argMissingException(MethodArgumentNotValidException ex) {

    ResponseStatus status =
        new ResponseStatus()
            .code(AppConstants.Status.ERROR)
            .errorCode(ResponseMessage.MANDATORY_FIELD_MISSING.getCode().toString())
            .errorMessage(ResponseMessage.MANDATORY_FIELD_MISSING.getMsg());

    return new ResponseEntity<>(new ResponseWrapper().status(status), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(GatewayTimeoutException.class)
  public ResponseEntity<ResponseWrapper> gatewayTimeoutException(GatewayTimeoutException ex) {

    // TODO update response detail later
    ResponseStatus status =
        new ResponseStatus()
            .code(AppConstants.Status.ERROR)
            .errorCode(ResponseMessage.INTERNAL_SERVER_ERROR.getCode().toString())
            .errorMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getMsg());

    return new ResponseEntity<>(new ResponseWrapper().status(status), HttpStatus.GATEWAY_TIMEOUT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseWrapper> genericException(Exception ex) {

    ResponseStatus status =
        new ResponseStatus()
            .code(AppConstants.Status.ERROR)
            .errorCode(ResponseMessage.INTERNAL_SERVER_ERROR.getCode().toString())
            .errorMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getMsg());

    return new ResponseEntity<>(
        new ResponseWrapper().status(status), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
