package com.rhbgroup.dte.obc.acount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.api.AccountApiController;
import com.rhbgroup.dte.obc.api.AccountApiDelegate;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.GlobalExceptionHandler;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest extends AbstractAccountTest {

  MockMvc mockMvc;

  @Mock AccountApiDelegate accountApiDelegate;

  @InjectMocks AccountApiController accountApiController;

  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(accountApiController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void testInitAccount_Success2xx() throws Exception {
    Mockito.when(accountApiDelegate.initLinkAccount(Mockito.any()))
        .thenReturn(ResponseEntity.ok(mockInitAccountResponse()));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/init-link-account")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockInitAccountRequest())))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());
  }

  @Test
  void testInitAccount_Failed_500() throws Exception {
    Mockito.when(accountApiDelegate.initLinkAccount(Mockito.any()))
        .thenThrow(new RuntimeException());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/init-link-account")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockInitAccountRequest())))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testInitAccount_Failed_Unauthorized_401() throws Exception {
    Mockito.when(accountApiDelegate.initLinkAccount(Mockito.any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/init-link-account")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockInitAccountRequest())))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testInitAccount_Failed_BadRequest_401() throws Exception {
    Mockito.when(accountApiDelegate.initLinkAccount(Mockito.any()))
        .thenThrow(new BizException(ResponseMessage.DATA_NOT_FOUND));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/init-link-account")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockInitAccountRequest())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testVerifyOtp_Success2xx() throws Exception {
    Mockito.when(accountApiDelegate.verifyOtp(Mockito.any()))
        .thenReturn(ResponseEntity.ok(mockVerifyOtpResponse()));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());
  }

  @Test
  void testVerifyOtp_Failed_Unauthorized_401() throws Exception {
    Mockito.when(accountApiDelegate.verifyOtp(Mockito.any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testVerifyOtp_Failed_500() throws Exception {
    Mockito.when(accountApiDelegate.verifyOtp(Mockito.any())).thenThrow(new RuntimeException());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testAuthenticate_Success_200() throws Exception {
    Mockito.when(accountApiDelegate.authenticate(Mockito.any()))
        .thenReturn(ResponseEntity.ok(mockAuthenticationResponse()));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockAuthenticationRequest())))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    AuthenticationResponse authResponse =
        objectMapper.readValue(contentAsString, AuthenticationResponse.class);

    Assertions.assertNotNull(authResponse.getStatus());
    Assertions.assertNotNull(authResponse.getData());

    Assertions.assertEquals(AppConstants.STATUS.SUCCESS, authResponse.getStatus().getCode());
    Assertions.assertEquals(mockJwtToken(), authResponse.getData().getAccessToken());
    Assertions.assertFalse(authResponse.getData().getRequireChangePassword());
  }

  @Test
  void testAuthenticate_Failed_Unauthorized_401() throws Exception {
    Mockito.when(accountApiDelegate.authenticate(Mockito.any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockAuthenticationRequest())))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    AuthenticationResponse authResponse =
        objectMapper.readValue(contentAsString, AuthenticationResponse.class);

    Assertions.assertNotNull(authResponse.getStatus());
    Assertions.assertNull(authResponse.getData());
    Assertions.assertEquals(AppConstants.STATUS.ERROR, authResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getMsg(), authResponse.getStatus().getErrorMessage());
    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getCode().toString(),
        authResponse.getStatus().getErrorCode());
  }

  @Test
  void testAuthenticate_Failed_MissingMandatoryFields_400() throws Exception {
    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(new AuthenticationRequest())))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    AuthenticationResponse authResponse =
        objectMapper.readValue(contentAsString, AuthenticationResponse.class);

    Assertions.assertNotNull(authResponse.getStatus());
    Assertions.assertNull(authResponse.getData());
    Assertions.assertEquals(AppConstants.STATUS.ERROR, authResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.MANDATORY_FIELD_MISSING.getMsg(),
        authResponse.getStatus().getErrorMessage());
    Assertions.assertEquals(
        ResponseMessage.MANDATORY_FIELD_MISSING.getCode().toString(),
        authResponse.getStatus().getErrorCode());
  }

  @Test
  void testAuthenticate_Failed_TokenExpired_401() throws Exception {
    Mockito.when(accountApiDelegate.authenticate(Mockito.any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.SESSION_EXPIRED));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockAuthenticationRequest())))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    AuthenticationResponse authResponse =
        objectMapper.readValue(contentAsString, AuthenticationResponse.class);

    Assertions.assertNotNull(authResponse.getStatus());
    Assertions.assertNull(authResponse.getData());
    Assertions.assertEquals(AppConstants.STATUS.ERROR, authResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.SESSION_EXPIRED.getMsg(), authResponse.getStatus().getErrorMessage());
    Assertions.assertEquals(
        ResponseMessage.SESSION_EXPIRED.getCode().toString(),
        authResponse.getStatus().getErrorCode());
  }

  @Test
  void testAuthenticate_Failed_InvalidToken_403() throws Exception {
    Mockito.when(accountApiDelegate.authenticate(Mockito.any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.INVALID_TOKEN));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockAuthenticationRequest())))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    AuthenticationResponse authResponse =
        objectMapper.readValue(contentAsString, AuthenticationResponse.class);

    Assertions.assertNotNull(authResponse.getStatus());
    Assertions.assertNull(authResponse.getData());
    Assertions.assertEquals(AppConstants.STATUS.ERROR, authResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.INVALID_TOKEN.getMsg(), authResponse.getStatus().getErrorMessage());
    Assertions.assertEquals(
        ResponseMessage.INVALID_TOKEN.getCode().toString(),
        authResponse.getStatus().getErrorCode());
  }
}
