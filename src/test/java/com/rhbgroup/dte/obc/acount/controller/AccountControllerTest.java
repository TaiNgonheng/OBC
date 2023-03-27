package com.rhbgroup.dte.obc.acount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.api.AccountApiController;
import com.rhbgroup.dte.obc.api.AccountApiDelegate;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.GlobalExceptionHandler;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  void testInitAccount_Failed_Unauthorized_400() throws Exception {
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
}
