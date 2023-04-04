package com.rhbgroup.dte.obc.domains.account.mapper;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.enums.BakongAccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.BakongKYCStatusEnum;
import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
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
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponseAllOfDataLimit;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import com.rhbgroup.dte.obc.model.UserModel;
import java.time.Instant;
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
      UserModel userModel, PGProfileResponse userProfile, String jwtToken, boolean otpEnabled) {

    InitAccountResponseAllOfData data =
        new InitAccountResponseAllOfData().accessToken(jwtToken).requireOtp(otpEnabled);

    if (!userProfile.getPhone().equals(userModel.getMobileNo())) {
      data.setRequireOtp(false);
      data.setRequireChangePhone(true);
      data.setLast3DigitsPhone(ObcStringUtils.getLast3DigitsPhone(userModel.getMobileNo()));

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
      AccountEntity entity, CDRBGetAccountDetailResponse accountDetailResponse) {
    CDRBGetAccountDetailResponseAcct accountDetail = accountDetailResponse.getAcct();

    entity.setAccountId(accountDetail.getAccountNo());
    entity.setAccountName(accountDetail.getAccountName());
    entity.setAccountType(accountDetail.getAccountType().getValue());
    entity.setAccountStatus(accountDetail.getAccountStatus().getValue());
    entity.setAccountCcy(accountDetail.getCurrencyCode());
    entity.setLinkedStatus(LinkedStatusEnum.COMPLETED);
    entity.setUpdatedDate(Instant.now());
    entity.setUpdatedBy(AppConstants.System.OPEN_BANKING_CLIENT);

    return entity;
  }

  default FinishLinkAccountResponse toFinishLinkAccountResponse() {
    return new FinishLinkAccountResponse()
        .status(ResponseHandler.ok())
        .data(new FinishLinkAccountResponseAllOfData().requireChangePassword(false));
  }

  @Mapping(source = "accountNo", target = "accNumber")
  @Mapping(source = "accountName", target = "accName")
  @Mapping(source = "accountType", target = "accType")
  @Mapping(source = "currencyCode", target = "accCcy")
  @Mapping(source = "accountStatus", target = "accStatus")
  @Mapping(source = "ctryCitizen", target = "country")
  @Mapping(source = "currentBal", target = "balance")
  GetAccountDetailResponseAllOfData toAccountDetailData(CDRBGetAccountDetailResponseAcct response);

  default GetAccountDetailResponse mappingMobileNoAndAccStatus(
      String mobileNo, Double trxMin, Double trxMax, GetAccountDetailResponse response) {

    GetAccountDetailResponseAllOfData responseData = response.getData();
    if (CDRBGetAccountDetailResponseAcct.AccountStatusEnum._1
            .name()
            .equals(responseData.getAccStatus())
        || CDRBGetAccountDetailResponseAcct.AccountStatusEnum._4
            .name()
            .equals(responseData.getAccStatus())
        || CDRBGetAccountDetailResponseAcct.AccountStatusEnum._5
            .name()
            .equals(responseData.getAccStatus())) {
      responseData.setAccStatus(BakongAccountStatusEnum.ACTIVE.name());
    } else {
      responseData.setAccStatus(BakongAccountStatusEnum.CLOSED.name());
    }

    if (CDRBGetAccountDetailResponseAcct.KycStatusEnum.F.getValue()
        .equals(responseData.getKycStatus())) {
      responseData.setKycStatus(BakongKYCStatusEnum.FULL.name());
    } else if (CDRBGetAccountDetailResponseAcct.KycStatusEnum.V.getValue()
            .equals(responseData.getKycStatus())
        || CDRBGetAccountDetailResponseAcct.KycStatusEnum.X.getValue()
            .equals(responseData.getKycStatus())) {
      responseData.setKycStatus(BakongKYCStatusEnum.PARTIAL.name());
    } else {
      responseData.setKycStatus(BakongKYCStatusEnum.BASIC.name());
    }

    responseData.setAccPhone(mobileNo);
    responseData.setLimit(
        new GetAccountDetailResponseAllOfDataLimit().maxTrxAmount(trxMax).minTrxAmount(trxMin));

    return response;
  }

  default GetAccountDetailResponse toAccountDetailResponse(
      CDRBGetAccountDetailResponseAcct response) {
    return new GetAccountDetailResponse()
        .status(ResponseHandler.ok())
        .data(toAccountDetailData(response));
  }
}
