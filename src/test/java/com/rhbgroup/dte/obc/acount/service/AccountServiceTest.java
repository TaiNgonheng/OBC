package com.rhbgroup.dte.obc.acount.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.common.util.CacheUtil;
import com.rhbgroup.dte.obc.domains.account.service.impl.AccountServiceImpl;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Optional;
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

  @Mock ConfigRepository configRepository;

  @Mock UserAuthService userAuthService;

  @Mock PGRestClient pgRestClient;

  @BeforeEach
  void cleanUp() {
    reset(jwtTokenUtils, cacheUtil, configRepository, userAuthService, pgRestClient);
  }

  @Test
  void testInitLinkAccount() {
    ConfigEntity configEntity = new ConfigEntity();
    configEntity.setRequiredTrxOtp(true);

    when(cacheUtil.getValueFromKey(anyString(), anyString())).thenReturn(mockJwtToken());
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(configRepository.getByServiceName(anyString())).thenReturn(Optional.of(configEntity));
    when(pgRestClient.getUserProfile(anyMap(), anyString())).thenReturn(mockProfileResponse());
    when(jwtTokenUtils.generateJwt(any())).thenReturn(mockJwtToken());

    InitAccountResponse response = accountService.initLinkAccount(mockInitAccountRequest());
    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
    Assertions.assertEquals(1, response.getData().getRequireOtp());
    Assertions.assertEquals(1, response.getData().getRequireChangePhone());
  }
}
