package com.rhbgroup.dte.obc.acount.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.domains.account.service.impl.AccountServiceImpl;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest extends AbstractAccountTest {

  @InjectMocks AccountServiceImpl accountService;

  @Mock JwtTokenUtils jwtTokenUtils;

  @Mock UserAuthService userAuthService;

  @Mock ConfigService configService;

  @Mock PGRestClient pgRestClient;

  @Mock InfoBipRestClient infoBipRestClient;

  @BeforeEach
  void cleanUp() {
    reset(jwtTokenUtils, configService, userAuthService, pgRestClient, infoBipRestClient);
  }

  @Test
  void testInitLinkAccount_Success_RequireChangePassword() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(configService.getByConfigKey(anyString(), anyString(), any())).thenReturn(1);
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileRequiredChangeMobile());
    when(jwtTokenUtils.generateJwt(any())).thenReturn(mockJwtToken());

    InitAccountResponse response = accountService.initLinkAccount(mockInitAccountRequest());
    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
    Assertions.assertEquals(true, response.getData().getRequireOtp());
    Assertions.assertEquals(true, response.getData().getRequireChangePhone());
  }

  @Test
  void testInitLinkAccount_Failed_AccountNotFullyKYC() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileNotFullyKyc());
    when(jwtTokenUtils.generateJwt(any())).thenReturn(mockJwtToken());

    try {
      accountService.initLinkAccount(mockInitAccountRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.KYC_NOT_VERIFIED.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.KYC_NOT_VERIFIED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitLinkAccount_Failed_AccountNotActive() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileUserDeactivated());
    when(jwtTokenUtils.generateJwt(any())).thenReturn(mockJwtToken());

    try {
      accountService.initLinkAccount(mockInitAccountRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.ACCOUNT_DEACTIVATED.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.ACCOUNT_DEACTIVATED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitLinkAccount_Success_NotRequireChangePassword() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(configService.getByConfigKey(anyString(), anyString(), any())).thenReturn(1);
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileNotRequiredChangeMobile());
    when(jwtTokenUtils.generateJwt(any())).thenReturn(mockJwtToken());
    when(infoBipRestClient.sendOtp(anyString(), anyString()))
        .thenReturn(mockInfoBipSendOtpResponse());

    InitAccountResponse response = accountService.initLinkAccount(mockInitAccountRequest());
    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
    Assertions.assertEquals(true, response.getData().getRequireOtp());
    Assertions.assertFalse(response.getData().getRequireChangePhone());
  }

  @Test
  void testInitLinkAccount_Failed_UserNotFound() {
    when(userAuthService.authenticate(any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    try {
      accountService.initLinkAccount(mockInitAccountRequest());
    } catch (UserAuthenticationException ex) {

      Assertions.assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitLinkAccount_Failed_3rdServiceUnavailable() {
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(pgRestClient.getUserProfile(anyList()))
        .thenThrow(new BizException(ResponseMessage.INTERNAL_SERVER_ERROR));

    try {
      accountService.initLinkAccount(mockInitAccountRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testInitLinkAccount_Failed_InfoBipServiceUnavailable() {
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileNotRequiredChangeMobile());
    when(infoBipRestClient.sendOtp(anyString(), anyString()))
        .thenThrow(new BizException(ResponseMessage.INTERNAL_SERVER_ERROR));

    try {
      accountService.initLinkAccount(mockInitAccountRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testVerifyOTP_Success_IsValid_True() {
    when(jwtTokenUtils.extractJwt(anyString())).thenReturn(mockJwtToken());
    when(jwtTokenUtils.getUsernameFromJwtToken(anyString())).thenReturn("username");
    when(infoBipRestClient.verifyOtp(anyString(), anyString())).thenReturn(true);

    VerifyOtpResponse response = accountService.verifyOtp(anyString(), mockVerifyOtpRequest());
    Assertions.assertEquals(AppConstants.STATUS.SUCCESS, response.getStatus().getCode());
    Assertions.assertTrue(response.getData().getIsValid());
  }

  @Test
  void testVerifyOTP_Success_IsValid_False() {
    when(jwtTokenUtils.extractJwt(anyString())).thenReturn(mockJwtToken());
    when(jwtTokenUtils.getUsernameFromJwtToken(anyString())).thenReturn("username");
    when(infoBipRestClient.verifyOtp(anyString(), anyString())).thenReturn(false);

    VerifyOtpResponse response = accountService.verifyOtp(anyString(), mockVerifyOtpRequest());
    Assertions.assertEquals(AppConstants.STATUS.SUCCESS, response.getStatus().getCode());
    Assertions.assertFalse(response.getData().getIsValid());
  }

  @Test
  void testVerifyOTP_Failed_InfoBipServiceUnavailable() {
    when(jwtTokenUtils.extractJwt(anyString())).thenReturn(mockJwtToken());
    when(jwtTokenUtils.getUsernameFromJwtToken(anyString())).thenReturn("username");
    when(infoBipRestClient.verifyOtp(anyString(), anyString()))
        .thenThrow(new BizException(ResponseMessage.INTERNAL_SERVER_ERROR));

    try {
      accountService.verifyOtp(anyString(), mockVerifyOtpRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getMsg(), ex.getResponseMessage().getMsg());
    }
  }
}
