package com.rhbgroup.dte.obc.domains.account.mapper;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.util.ObcStringUtils;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.AuthenticationResponseAllOfData;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponseAcct;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.UserModel;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AccountMapper {

  @Mapping(source = "phoneNumber", target = "mobileNo")
  @Mapping(source = "bakongAccId", target = "bakongId")
  @Mapping(source = "login", target = "user.username")
  @Mapping(source = "key", target = "user.password")
  @Mapping(source = "loginType", target = "user.loginType")
  AccountModel toModel(InitAccountRequest request);

  default InitAccountResponse toInitAccountResponse(
      InitAccountRequest request,
      PGProfileResponse userProfile,
      String jwtToken,
      boolean otpEnabled) {

    InitAccountResponseAllOfData data =
        new InitAccountResponseAllOfData().accessToken(jwtToken).requireOtp(otpEnabled);

    if (!userProfile.getPhone().equals(request.getPhoneNumber())) {
      data.setRequireChangePhone(true);
      data.setLast3DigitsPhone(ObcStringUtils.getLast3DigitsPhone(userProfile.getPhone()));
    } else {
      data.setRequireChangePhone(false);
    }

    return new InitAccountResponse().status(ResponseHandler.ok()).data(data);
  }

  @Mapping(source = "login", target = "username")
  @Mapping(source = "key", target = "password")
  UserModel toUserModel(AuthenticationRequest request);

  default AuthenticationResponse toAuthResponse(String token) {
    AuthenticationResponseAllOfData responseData =
        new AuthenticationResponseAllOfData().accessToken(token).requireChangePassword(false);
    return new AuthenticationResponse().status(ResponseHandler.ok()).data(responseData);
  }

  default AccountEntity toAccountEntity(
      Long userId, CDRBGetAccountDetailResponse accountDetailResponse) {
    AccountEntity accountEntity = new AccountEntity();
    CDRBGetAccountDetailResponseAcct accountDetail = accountDetailResponse.getAcct();

    accountEntity.setUserId(userId);
    accountEntity.setAccountId(accountDetail.getAccountNo());
    accountEntity.setAccountName(accountDetail.getAccountName());
    accountEntity.setAccountType(accountDetail.getAccountType().getValue());
    accountEntity.setAccountStatus(accountDetail.getAccountStatus().getValue());
    accountEntity.setAccountCcy(accountDetail.getCurrencyCode());
    accountEntity.setCountry(accountDetail.getCtryCitizen());
    accountEntity.setBalance(BigDecimal.valueOf(accountDetail.getCurrentBal()));

    return accountEntity;
  }

  default FinishLinkAccountResponse toFinishLinkAccountResponse() {
    return new FinishLinkAccountResponse()
        .status(ResponseHandler.ok())
        .data(new FinishLinkAccountResponseAllOfData().requireChangePassword(false));
  }
}
