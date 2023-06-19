package com.rhbgroup.dte.obc.acount.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.domains.account.service.impl.AccountServiceImpl;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.config.service.impl.ConfigServiceImpl;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.InternalException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.BakongAccountStatus;
import com.rhbgroup.dte.obc.model.BakongKYCStatus;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.UnlinkAccountResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
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

  @Mock CDRBRestClient cdrbRestClient;

  @Mock UserProfileService userProfileService;

  @Mock AccountRepository accountRepository;

  @BeforeEach
  void cleanUp() {
    reset(
        jwtTokenUtils,
        configService,
        userAuthService,
        pgRestClient,
        infoBipRestClient,
        cdrbRestClient,
        accountRepository,
        userProfileService);
  }

  @Test
  void testInitLinkAccount_Success_RequireChangePhone() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(userProfileService.findByUsername(anyString())).thenReturn(mockUserModel());
    when(accountRepository.save(any(AccountEntity.class))).thenReturn(new AccountEntity());
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileRequiredChangeMobile());
    when(jwtTokenUtils.generateJwtAppUser(anyString(), any())).thenReturn(mockJwtToken());

    // Set to other phone number
    InitAccountRequest initAccountRequest = mockInitAccountRequest();
    initAccountRequest.setPhoneNumber("85512862862");
    InitAccountResponse response = accountService.initLinkAccount(initAccountRequest);

    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
    Assertions.assertEquals(true, response.getData().getRequireChangePhone());
  }

  @Test
  void testInitLinkAccount_Success_ValueInCacheHasBeenExpired() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(userProfileService.findByUsername(anyString())).thenReturn(mockUserModel());
    when(accountRepository.save(any(AccountEntity.class))).thenReturn(new AccountEntity());
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileRequiredChangeMobile());
    when(jwtTokenUtils.generateJwtAppUser(anyString(), any())).thenReturn(mockJwtToken());

    InitAccountResponse response = accountService.initLinkAccount(mockInitAccountRequest());
    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
    Assertions.assertEquals(false, response.getData().getRequireChangePhone());
  }

  @Test
  void testInitLinkAccount_Failed_AccountNotFullyKYC() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileNotFullyKyc());
    when(jwtTokenUtils.generateJwtAppUser(anyString(), any())).thenReturn(mockJwtToken());

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
    when(jwtTokenUtils.generateJwtAppUser(anyString(), any())).thenReturn(mockJwtToken());

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
  void testInitLinkAccount_Success_NotRequireChangePhone() {

    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(userProfileService.findByUsername(anyString())).thenReturn(mockUserModel());
    when(accountRepository.save(any(AccountEntity.class))).thenReturn(new AccountEntity());
    when(pgRestClient.getUserProfile(anyList())).thenReturn(mockProfileNotRequiredChangeMobile());
    when(jwtTokenUtils.generateJwtAppUser(anyString(), any())).thenReturn(mockJwtToken());
    when(infoBipRestClient.sendOtp(anyString(), anyString()))
        .thenReturn(mockInfoBipSendOtpResponse());

    InitAccountResponse response = accountService.initLinkAccount(mockInitAccountRequest());
    Assertions.assertEquals(0, response.getStatus().getCode());
    Assertions.assertEquals(response.getData().getAccessToken(), mockJwtToken());
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
        .thenThrow(new InternalException(ResponseMessage.INTERNAL_SERVER_ERROR));

    try {
      accountService.initLinkAccount(mockInitAccountRequest());
    } catch (InternalException ex) {
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
    when(userProfileService.findByUsername(anyString())).thenReturn(mockUserModel());
    when(infoBipRestClient.sendOtp(anyString(), anyString()))
        .thenThrow(new InternalException(ResponseMessage.INTERNAL_SERVER_ERROR));

    try {
      accountService.initLinkAccount(mockInitAccountRequest());
    } catch (InternalException ex) {
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testVerifyOTP_Success_IsValid_True() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(infoBipRestClient.verifyOtp(anyString(), anyString())).thenReturn(true);
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());

    VerifyOtpResponse response = accountService.verifyOtp(mockVerifyOtpRequest());
    Assertions.assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    Assertions.assertTrue(response.getData().getIsValid());
  }

  @Test
  void testVerifyOTP_Success_IsValid_False() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(infoBipRestClient.verifyOtp(anyString(), anyString())).thenReturn(false);

    VerifyOtpResponse response = accountService.verifyOtp(mockVerifyOtpRequest());
    Assertions.assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    Assertions.assertFalse(response.getData().getIsValid());
  }

  @Test
  void testVerifyOTP_Failed_InfoBipServiceUnavailable() {
    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(infoBipRestClient.verifyOtp(anyString(), anyString()))
        .thenThrow(new InternalException(ResponseMessage.INTERNAL_SERVER_ERROR));

    try {
      accountService.verifyOtp(mockVerifyOtpRequest());
    } catch (InternalException ex) {
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.INTERNAL_SERVER_ERROR.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testAuthenticate_Successful() {
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(jwtTokenUtils.generateJwtAppUser(any())).thenReturn(mockJwtToken());
    when(accountRepository.findFirstByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.of(mockAccountEntityLinked()));

    AuthenticationResponse response = accountService.authenticate(mockAuthenticationRequest());

    Assertions.assertNotNull(response.getData());
    Assertions.assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    Assertions.assertEquals(mockJwtToken(), response.getData().getAccessToken());
    Assertions.assertEquals(mockJwtToken(), response.getData().getAccessToken());
    Assertions.assertFalse(response.getData().getRequireChangePassword());
  }

  @Test
  void testAuthenticate_Failed_Unauthorized() {
    when(userAuthService.authenticate(any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    try {
      accountService.authenticate(mockAuthenticationRequest());
    } catch (UserAuthenticationException ex) {
      Assertions.assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testAuthenticate_Failed_Unauthorized_ROLE_NOT_PERMITTED() {
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(accountRepository.findFirstByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.of(mockAccountEntityLinked()));
    doThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED))
        .when(userAuthService)
        .checkUserRole(any(), anyList());

    try {
      accountService.authenticate(mockAuthenticationRequest());
    } catch (UserAuthenticationException ex) {
      Assertions.assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.AUTHENTICATION_FAILED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testAuthenticate_Failed_AccountNotActive() {
    when(userAuthService.authenticate(any())).thenReturn(mockAuthentication());
    when(accountRepository.findFirstByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.of(mockAccountEntityAccountPending()));
    try {
      accountService.authenticate(mockAuthenticationRequest());
    } catch (UserAuthenticationException ex) {
      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishLinkAccount_Success_WithBrandNewAccount() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());
    when(accountRepository.findByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.ofNullable(mockAccountEntityAccountPending()));
    when(accountRepository.save(any(AccountEntity.class))).thenReturn(mockAccountEntityLinked());

    FinishLinkAccountResponse response =
        accountService.finishLinkAccount(new FinishLinkAccountRequest().accNumber("12345"));

    Assertions.assertNotNull(response);
    Assertions.assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    Assertions.assertFalse(response.getData().getRequireChangePassword());
  }

  @Test
  void testFinishLinkAccount_Success_AccountAlreadyLinked() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(accountRepository.findByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.ofNullable(mockAccountEntityLinked()));
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());

    try {
      accountService.finishLinkAccount(new FinishLinkAccountRequest().accNumber("12345"));
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.ACCOUNT_ALREADY_LINKED.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.ACCOUNT_ALREADY_LINKED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishLinkAccount_Failed_FetchAccountError() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(accountRepository.findByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.ofNullable(mockAccountEntityAccountPending()));
    when(cdrbRestClient.getAccountDetail(any()))
        .thenThrow(new BizException(ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS));

    try {
      accountService.finishLinkAccount(mockFinishLinkAccountRequest());

    } catch (BizException ex) {

      Assertions.assertEquals(
          ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS.getCode(),
          ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishLinkAccount_Failed_AccountNotFullyKyc() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(accountRepository.findByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.ofNullable(mockAccountEntityAccountPending()));
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponseNotKYC());

    try {
      accountService.finishLinkAccount(mockFinishLinkAccountRequest());

    } catch (BizException ex) {

      Assertions.assertEquals(
          ResponseMessage.KYC_NOT_VERIFIED.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.KYC_NOT_VERIFIED.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testFinishLinkAccount_Failed_AccountNotFoundInOBC() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(accountRepository.findByUserIdAndBakongIdAndLinkedStatus(any(), anyString(), any()))
        .thenReturn(Optional.empty());

    try {
      accountService.finishLinkAccount(mockFinishLinkAccountRequest());
    } catch (BizException ex) {

      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testUnlinkAccount_Success() {
    when(accountRepository.findByAccountIdAndLinkedStatus(anyString(), any()))
        .thenReturn(Optional.of(mockAccountEntityLinked()));

    UnlinkAccountResponse response = accountService.unlinkAccount(mockUnlinkAccountRequest());

    Assertions.assertEquals(0, response.getStatus().getCode());
  }

  @Test
  void testUnlinkAccount_Failed_AccountNotFound() {
    when(accountRepository.findByAccountIdAndLinkedStatus(anyString(), any()))
        .thenReturn(Optional.empty());

    try {
      accountService.unlinkAccount(mockUnlinkAccountRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testGetAccountDetail_Success() throws JSONException {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(accountRepository.countByAccountIdAndLinkedStatus(anyString(), any())).thenReturn(1L);
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponse());

    ConfigServiceImpl mockConfig = new ConfigServiceImpl(null);

    Double maxAmt = 1000.0;
    Double minAmt = 1.0;
    mockConfig.setJsonValue(new JSONObject().put("txMinAmt", minAmt).put("txMaxAmt", maxAmt));

    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockConfig);

    GetAccountDetailResponse response =
        accountService.getAccountDetail(mockGetAccountDetailRequest());

    Assertions.assertNotNull(response.getData());
    Assertions.assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    Assertions.assertEquals(BakongAccountStatus.ACTIVE, response.getData().getAccStatus());
    Assertions.assertEquals(BakongKYCStatus.FULL, response.getData().getKycStatus());
    Assertions.assertEquals(minAmt, response.getData().getLimit().getMinTrxAmount());
    Assertions.assertEquals(maxAmt, response.getData().getLimit().getMaxTrxAmount());
  }

  @Test
  void testGetAccountDetail_Success_AccountNotFullyKYC() throws JSONException {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(accountRepository.countByAccountIdAndLinkedStatus(anyString(), any())).thenReturn(1L);
    when(cdrbRestClient.getAccountDetail(any())).thenReturn(mockCdrbAccountResponseNotKYC());

    ConfigServiceImpl mockConfig = new ConfigServiceImpl(null);

    Double maxAmt = 1000.0;
    Double minAmt = 1.0;
    mockConfig.setJsonValue(new JSONObject().put("txMinAmt", minAmt).put("txMaxAmt", maxAmt));

    when(configService.loadJSONValue(ConfigConstants.Transaction.CONFIG_KEY_USD))
        .thenReturn(mockConfig);

    GetAccountDetailResponse response =
        accountService.getAccountDetail(mockGetAccountDetailRequest());

    Assertions.assertNotNull(response.getData());
    Assertions.assertEquals(AppConstants.Status.SUCCESS, response.getStatus().getCode());
    Assertions.assertEquals(BakongAccountStatus.BLOCKED, response.getData().getAccStatus());
    Assertions.assertEquals(BakongKYCStatus.PARTIAL, response.getData().getKycStatus());
    Assertions.assertEquals(minAmt, response.getData().getLimit().getMinTrxAmount());
    Assertions.assertEquals(maxAmt, response.getData().getLimit().getMaxTrxAmount());
  }

  @Test
  void testGetAccountDetail_Failed_NoAccountFound() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(accountRepository.countByAccountIdAndLinkedStatus(anyString(), any())).thenReturn(0L);

    try {
      accountService.getAccountDetail(mockGetAccountDetailRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getCode(), ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.NO_ACCOUNT_FOUND.getMsg(), ex.getResponseMessage().getMsg());
    }
  }

  @Test
  void testGetAccountDetail_FailedWhenFetchingCasaAccountDetail() {

    when(userAuthService.getCurrentUser()).thenReturn(mockCustomUserDetails());
    when(userProfileService.findByUserId(any())).thenReturn(mockUserModel());
    when(accountRepository.countByAccountIdAndLinkedStatus(anyString(), any())).thenReturn(1L);
    when(cdrbRestClient.getAccountDetail(any()))
        .thenThrow(new BizException(ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS));

    try {
      accountService.getAccountDetail(mockGetAccountDetailRequest());
    } catch (BizException ex) {
      Assertions.assertEquals(
          ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS.getCode(),
          ex.getResponseMessage().getCode());
      Assertions.assertEquals(
          ResponseMessage.FAIL_TO_FETCH_ACCOUNT_DETAILS.getMsg(), ex.getResponseMessage().getMsg());
    }
  }
}
