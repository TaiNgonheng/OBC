package com.rhbgroup.dte.obc.acount.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.api.AccountApiController;
import com.rhbgroup.dte.obc.api.AccountApiDelegate;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.GlobalExceptionHandler;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.BakongAccountStatus;
import com.rhbgroup.dte.obc.model.BakongKYCStatus;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.UnlinkAccountResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
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

    reset(accountApiDelegate);
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
                .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());
  }

  @Test
  void testVerifyOtp_Failed_DataNotFound_400() throws Exception {
    Mockito.when(accountApiDelegate.verifyOtp(Mockito.any()))
        .thenThrow(new BizException(ResponseMessage.DATA_NOT_FOUND));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/verify-otp")
                .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testVerifyOtp_Failed_Unauthorized_401() throws Exception {
    Mockito.when(accountApiDelegate.verifyOtp(Mockito.any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/verify-otp")
                .header(HttpHeaders.AUTHORIZATION, mockBearerString())
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
                .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testVerifyOtp_Failed_TokenExpired_401() throws Exception {
    Mockito.when(accountApiDelegate.verifyOtp(any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.SESSION_EXPIRED));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/verify-otp")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
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
    Assertions.assertEquals(AppConstants.Status.ERROR, authResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.SESSION_EXPIRED.getMsg(), authResponse.getStatus().getErrorMessage());
    Assertions.assertEquals(
        ResponseMessage.SESSION_EXPIRED.getCode().toString(),
        authResponse.getStatus().getErrorCode());
  }

  @Test
  void testVerifyOtp_Failed_InvalidToken_403() throws Exception {
    Mockito.when(accountApiDelegate.verifyOtp(any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.INVALID_TOKEN));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/verify-otp")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockVerifyOtpRequest())))
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
    Assertions.assertEquals(AppConstants.Status.ERROR, authResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.INVALID_TOKEN.getMsg(), authResponse.getStatus().getErrorMessage());
    Assertions.assertEquals(
        ResponseMessage.INVALID_TOKEN.getCode().toString(),
        authResponse.getStatus().getErrorCode());
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

    Assertions.assertEquals(AppConstants.Status.SUCCESS, authResponse.getStatus().getCode());
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
    Assertions.assertEquals(AppConstants.Status.ERROR, authResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getMsg(), authResponse.getStatus().getErrorMessage());
    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getCode().toString(),
        authResponse.getStatus().getErrorCode());
  }

  @Test
  void testFinishLinkAccount_Success_200() throws Exception {
    when(accountApiDelegate.finishLinkAccount(any()))
        .thenReturn(ResponseEntity.ok(mockFinishLinkAccountResponse()));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/finish-link-account")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockFinishLinkAccountRequest())))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    FinishLinkAccountResponse finishLinkAccountResponse =
        objectMapper.readValue(contentAsString, FinishLinkAccountResponse.class);

    Assertions.assertNotNull(finishLinkAccountResponse.getStatus());
    Assertions.assertNotNull(finishLinkAccountResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, finishLinkAccountResponse.getStatus().getCode());
    Assertions.assertFalse(finishLinkAccountResponse.getData().getRequireChangePassword());
  }

  @Test
  void testFinishLinkAccount_Failed_KycNotVerified_400() throws Exception {
    when(accountApiDelegate.finishLinkAccount(any()))
        .thenThrow(new BizException(ResponseMessage.KYC_NOT_VERIFIED));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/finish-link-account")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockFinishLinkAccountRequest())))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    FinishLinkAccountResponse finishLinkAccountResponse =
        objectMapper.readValue(contentAsString, FinishLinkAccountResponse.class);

    Assertions.assertNotNull(finishLinkAccountResponse.getStatus());
    Assertions.assertNull(finishLinkAccountResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.ERROR, finishLinkAccountResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.KYC_NOT_VERIFIED.getCode().toString(),
        finishLinkAccountResponse.getStatus().getErrorCode());
    Assertions.assertEquals(
        ResponseMessage.KYC_NOT_VERIFIED.getMsg(),
        finishLinkAccountResponse.getStatus().getErrorMessage());
  }

  @Test
  void testUnlinkAccount_Success_200() throws Exception {
    when(accountApiDelegate.unlinkAccount(any()))
        .thenReturn(ResponseEntity.ok(mockUnlinkAccountResponse()));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/unlink-account")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockUnlinkAccountRequest())))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    UnlinkAccountResponse unlinkAccountResponse =
        objectMapper.readValue(contentAsString, UnlinkAccountResponse.class);

    Assertions.assertNotNull(unlinkAccountResponse.getStatus());
    Assertions.assertNull(unlinkAccountResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, unlinkAccountResponse.getStatus().getCode());
  }

  @Test
  void testUnlinkAccount_Failed_MissingMandatoryFields_400() throws Exception {
    when(accountApiDelegate.unlinkAccount(any()))
        .thenThrow(new BizException(ResponseMessage.MANDATORY_FIELD_MISSING));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/unlink-account")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockUnlinkAccountRequest())))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    UnlinkAccountResponse unlinkAccountResponse =
        objectMapper.readValue(contentAsString, UnlinkAccountResponse.class);

    Assertions.assertNotNull(unlinkAccountResponse.getStatus());
    Assertions.assertNull(unlinkAccountResponse.getData());

    Assertions.assertEquals(AppConstants.Status.ERROR, unlinkAccountResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.MANDATORY_FIELD_MISSING.getCode().toString(),
        unlinkAccountResponse.getStatus().getErrorCode());
    Assertions.assertEquals(
        ResponseMessage.MANDATORY_FIELD_MISSING.getMsg(),
        unlinkAccountResponse.getStatus().getErrorMessage());
  }

  @Test
  void testUnlinkAccount_Failed_Unauthorized_401() throws Exception {
    when(accountApiDelegate.unlinkAccount(any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/unlink-account")
                .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsBytes(mockUnlinkAccountRequest())))
        .andExpect(MockMvcResultMatchers.status().isUnauthorized())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
  }

  @Test
  void testGetAccountDetail_Success_200() throws Exception {

    when(accountApiDelegate.getAccountDetail(any()))
        .thenReturn(ResponseEntity.ok(mockGetAccountDetailResponse()));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/account-detail")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockGetAccountDetailRequest())))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    GetAccountDetailResponse accountDetailResponse =
        objectMapper.readValue(contentAsString, GetAccountDetailResponse.class);

    Assertions.assertNotNull(accountDetailResponse.getStatus());
    Assertions.assertNotNull(accountDetailResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, accountDetailResponse.getStatus().getCode());
    Assertions.assertEquals(ACC_NUMBER, accountDetailResponse.getData().getAccNumber());
    Assertions.assertEquals(BakongKYCStatus.FULL, accountDetailResponse.getData().getKycStatus());
    Assertions.assertEquals(
        BakongAccountStatus.ACTIVE, accountDetailResponse.getData().getAccStatus());
  }

  @Test
  void testGetAccountDetail_Failed_400_AccountNotFound() throws Exception {

    when(accountApiDelegate.getAccountDetail(any()))
        .thenThrow(new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/account-detail")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockGetAccountDetailRequest())))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    GetAccountDetailResponse accountDetailResponse =
        objectMapper.readValue(contentAsString, GetAccountDetailResponse.class);

    Assertions.assertNotNull(accountDetailResponse.getStatus());
    Assertions.assertNull(accountDetailResponse.getData());

    Assertions.assertEquals(AppConstants.Status.ERROR, accountDetailResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.NO_ACCOUNT_FOUND.getCode().toString(),
        accountDetailResponse.getStatus().getErrorCode());
    Assertions.assertEquals(
        ResponseMessage.NO_ACCOUNT_FOUND.getMsg(),
        accountDetailResponse.getStatus().getErrorMessage());
  }

  @Test
  void testGetAccountDetail_Failed_400_FetchCasaAccountError() throws Exception {

    when(accountApiDelegate.getAccountDetail(any()))
        .thenThrow(new BizException(ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/account-detail")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockGetAccountDetailRequest())))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    GetAccountDetailResponse accountDetailResponse =
        objectMapper.readValue(contentAsString, GetAccountDetailResponse.class);

    Assertions.assertNotNull(accountDetailResponse.getStatus());
    Assertions.assertNull(accountDetailResponse.getData());

    Assertions.assertEquals(AppConstants.Status.ERROR, accountDetailResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS.getCode().toString(),
        accountDetailResponse.getStatus().getErrorCode());
    Assertions.assertEquals(
        ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS.getMsg(),
        accountDetailResponse.getStatus().getErrorMessage());
  }
}
