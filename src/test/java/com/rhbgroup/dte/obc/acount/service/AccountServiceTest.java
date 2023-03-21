package com.rhbgroup.dte.obc.acount.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.domains.account.service.impl.AccountServiceImpl;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
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

  @Mock CacheUtil cacheUtil;

  @Mock UserAuthService userAuthService;

  @Mock ConfigService configService;

  @Mock PGRestClient pgRestClient;

  @BeforeEach
  void cleanUp() {
    reset(jwtTokenUtils, cacheUtil, configService, userAuthService, pgRestClient);
  }

  @Test
  void testInitLinkAccount_Success_RequireChangePassword() {

    when(cacheUtil.getValueFromKey(anyString(), anyString())).thenReturn(mockJwtToken());
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(configService.getByConfigKey(anyString(), anyString(), any())).thenReturn(1);
    when(pgRestClient.getUserProfile(anyList(), anyString()))
        .thenReturn(mockProfileRequiredChangeMobile());
    when(jwtTokenUtils.generateJwt(any())).thenReturn(mockJwtToken());

    InitAccountResponse response = accountService.initLinkAccount(mockInitAccountRequest());
    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
    Assertions.assertEquals(1, response.getData().getRequireOtp());
    Assertions.assertEquals(1, response.getData().getRequireChangePhone());
  }

  @Test
  void testInitLinkAccount_Success_NotRequireChangePassword() {
    when(cacheUtil.getValueFromKey(anyString(), anyString())).thenReturn(mockJwtToken());
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(configService.getByConfigKey(anyString(), anyString(), any())).thenReturn(1);
    when(pgRestClient.getUserProfile(anyList(), anyString()))
        .thenReturn(mockProfileNotRequiredChangeMobile());
    when(jwtTokenUtils.generateJwt(any())).thenReturn(mockJwtToken());

    InitAccountResponse response = accountService.initLinkAccount(mockInitAccountRequest());
    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
    Assertions.assertEquals(1, response.getData().getRequireOtp());
    Assertions.assertNull(response.getData().getRequireChangePhone());
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
    when(cacheUtil.getValueFromKey(anyString(), anyString())).thenReturn(mockJwtToken());
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(pgRestClient.getUserProfile(anyList(), anyString()))
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
}
