package com.rhbgroup.dte.obc.domains.account.service.impl;

import static com.rhbgroup.dte.obc.common.func.Functions.of;
import static com.rhbgroup.dte.obc.common.func.Functions.peek;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapper;
import com.rhbgroup.dte.obc.domains.account.mapper.AccountMapperImpl;
import com.rhbgroup.dte.obc.domains.account.repository.AccountRepository;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.domains.account.service.AccountService;
import com.rhbgroup.dte.obc.domains.account.service.AccountValidator;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.user.service.UserAuthService;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.AccountFilterCondition;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.FinishLinkAccountRequest;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailRequest;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.UnlinkAccountRequest;
import com.rhbgroup.dte.obc.model.UnlinkAccountResponse;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.model.VerifyOtpRequest;
import com.rhbgroup.dte.obc.model.VerifyOtpResponse;
import com.rhbgroup.dte.obc.model.VerifyOtpResponseAllOfData;
import com.rhbgroup.dte.obc.rest.CDRBRestClient;
import com.rhbgroup.dte.obc.rest.InfoBipRestClient;
import com.rhbgroup.dte.obc.rest.PGRestClient;
import com.rhbgroup.dte.obc.security.CustomUserDetails;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final JwtTokenUtils jwtTokenUtils;

  private final UserAuthService userAuthService;
  private final UserProfileService userProfileService;
  private final ConfigService configService;

  private final AccountRepository accountRepository;

  private final PGRestClient pgRestClient;
  private final InfoBipRestClient infoBipRestClient;
  private final CDRBRestClient cdrbRestClient;

  private final AccountMapper accountMapper = new AccountMapperImpl();

  @Value("${obc.infobip.enabled}")
  protected boolean otpEnabled;

  @Override
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    return of(accountMapper::toUserModel)
        .andThen(userAuthService::authenticate)
        .andThen(
            peek(
                authContext -> {
                  // Checking account status
                  CustomUserDetails principal = (CustomUserDetails) authContext.getPrincipal();
                  if (!principal.isOtpVerified()) {
                    log.error("User {} is not fully KYC", principal.getUserId());
                    throw new BizException(ResponseMessage.AUTHENTICATION_FAILED);
                  }
                  Optional<AccountEntity> activeAccount =
                      accountRepository.findFirstByUserIdAndBakongIdAndLinkedStatus(
                          principal.getUserId(),
                          principal.getBakongId(),
                          LinkedStatusEnum.COMPLETED);
                  if (activeAccount.isEmpty()) {
                    log.error(
                        "No active account found for user {} with bakong id {}",
                        principal.getUserId(),
                        principal.getBakongId());
                    throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
                  }
                }))
        .andThen(
            peek(
                authContext ->
                    userAuthService.checkUserRole(
                        authContext, Collections.singletonList(AppConstants.Role.APP_USER))))
        .andThen(jwtTokenUtils::generateJwtAppUser)
        .andThen(accountMapper::toAuthResponse)
        .apply(request);
  }

  @Override
  public InitAccountResponse initLinkAccount(InitAccountRequest request) {

    // Generate OBC token
    String token =
        of(accountMapper::toModel)
            .andThen(AccountModel::getUser)
            .andThen(userAuthService::authenticate)
            .andThen(
                authContext ->
                    jwtTokenUtils.generateJwtAppUser(request.getBakongAccId(), authContext))
            .apply(request);

    // Get PG user profile, trigger OTP and build response
    return of(pgRestClient::getUserProfile)
        .andThen(peek(AccountValidator::validateAccount))
        .andThen(
            peek(
                userProfile ->
                    infoBipRestClient.sendOtp(userProfile.getPhone(), request.getLogin())))
        .andThen(peek(response -> insertBakongId(request.getLogin(), request.getBakongAccId())))
        .andThen(
            profileResponse -> {
              UserModel gowaveUser = userProfileService.findByUsername(request.getLogin());
              return accountMapper.toInitAccountResponse(
                  gowaveUser, profileResponse, token, otpEnabled);
            })
        .apply(Collections.singletonList(request.getBakongAccId()));
  }

  private void insertBakongId(String username, String bakongId) {

    of(userProfileService::findByUsername)
        .andThen(
            userModel ->
                accountRepository
                    .findByUserIdAndBakongIdAndLinkedStatus(
                        userModel.getId().longValue(), bakongId, LinkedStatusEnum.PENDING)
                    .orElseGet(
                        () -> {
                          AccountEntity accountEntity = new AccountEntity();
                          accountEntity.setUserId(userModel.getId().longValue());
                          accountEntity.setBakongId(bakongId);
                          accountEntity.setLinkedStatus(LinkedStatusEnum.PENDING);

                          return accountEntity;
                        }))
        .andThen(accountRepository::save)
        .apply(username);
  }

  @Override
  public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    boolean otpVerified =
        infoBipRestClient.verifyOtp(request.getOtpCode(), currentUser.getBakongId());

    if (otpVerified) {
      // Update otp verify status
      of(userProfileService::findByUserId)
          .andThen(
              peek(
                  userProfile -> {
                    userProfile.setOtpVerifiedStatus(true);
                    userProfile.setOtpVerifiedDate(OffsetDateTime.now());
                    userProfileService.updateUserProfile(userProfile);
                  }))
          .apply(currentUser.getUserId());
    }

    return new VerifyOtpResponse()
        .status(ResponseHandler.ok())
        .data(new VerifyOtpResponseAllOfData().isValid(otpVerified));
  }

  @Override
  public FinishLinkAccountResponse finishLinkAccount(FinishLinkAccountRequest request) {

    CustomUserDetails currentUser = userAuthService.getCurrentUser();
    if (StringUtils.isBlank(currentUser.getBakongId())) {
      throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
    }

    // One pending at a time
    AccountEntity pendingAccount =
        accountRepository
            .findByUserIdAndBakongIdAndLinkedStatus(
                currentUser.getUserId(), currentUser.getBakongId(), LinkedStatusEnum.PENDING)
            .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

    CDRBGetAccountDetailRequest accountDetailRequest =
        new CDRBGetAccountDetailRequest()
            .accountNo(request.getAccNumber())
            .cifNo(userProfileService.findByUserId(currentUser.getUserId()).getCifNo());

    // Validate if acct has been linked already
    accountRepository
        .findByAccountIdAndLinkedStatus(request.getAccNumber(), LinkedStatusEnum.COMPLETED)
        .ifPresent(
            account -> {
              throw new BizException(ResponseMessage.ACCOUNT_ALREADY_LINKED);
            });

    // Get CDRB account detail & update account table
    return of(cdrbRestClient::getAccountDetail)
        .andThen(peek(AccountValidator::validateCasaAccount))
        .andThen(cdrbAccount -> accountMapper.toAccountEntity(pendingAccount, cdrbAccount))
        .andThen(peek(accountRepository::save))
        .andThen(account -> accountMapper.toFinishLinkAccountResponse())
        .apply(accountDetailRequest);
  }

  @Override
  public GetAccountDetailResponse getAccountDetail(GetAccountDetailRequest request) {

    UserModel userModel =
        userProfileService.findByUserId(userAuthService.getCurrentUser().getUserId());

    Long accountNumber =
        accountRepository.countByAccountIdAndLinkedStatus(
            request.getAccNumber(), LinkedStatusEnum.COMPLETED);
    if (accountNumber == 0) {
      throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
    }

    return of(cdrbRestClient::getAccountDetail)
        .andThen(CDRBGetAccountDetailResponse::getAcct)
        .andThen(accountMapper::toAccountDetailResponse)
        .andThen(
            casaAccountResponse -> {
              ConfigService transactionConfig =
                  this.configService.loadJSONValue(
                      ConfigConstants.Transaction.mapCurrency(
                          casaAccountResponse.getData().getAccCcy()));

              return accountMapper.mappingMobileNoAndAccStatus(
                  userModel.getMobileNo(),
                  transactionConfig.getValue(ConfigConstants.Transaction.MIN_AMOUNT, Double.class),
                  transactionConfig.getValue(ConfigConstants.Transaction.MAX_AMOUNT, Double.class),
                  casaAccountResponse);
            })
        .apply(
            new CDRBGetAccountDetailRequest()
                .cifNo(userModel.getCifNo())
                .accountNo(request.getAccNumber()));
  }

  @Override
  public AccountModel getActiveAccount(AccountFilterCondition condition) {

    return accountRepository
        .findByAccountIdAndLinkedStatus(condition.getAccountNo(), LinkedStatusEnum.COMPLETED)
        .map(accountMapper::entityToModel)
        .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));
  }

  @Override
  @Transactional
  public UnlinkAccountResponse unlinkAccount(UnlinkAccountRequest unlinkAccountRequest) {

    AccountEntity accountEntity =
        accountRepository
            .findByAccountIdAndLinkedStatus(
                unlinkAccountRequest.getAccNumber(), LinkedStatusEnum.COMPLETED)
            .orElseThrow(() -> new BizException(ResponseMessage.NO_ACCOUNT_FOUND));

    accountEntity.setLinkedStatus(LinkedStatusEnum.UNLINKED);
    accountRepository.save(accountEntity);

    return new UnlinkAccountResponse().status(ResponseHandler.ok()).data(null);
  }
}
