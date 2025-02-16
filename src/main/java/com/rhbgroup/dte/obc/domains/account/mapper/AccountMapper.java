package com.rhbgroup.dte.obc.domains.account.mapper;

import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.common.util.ObcStringUtils;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.AuthenticationRequest;
import com.rhbgroup.dte.obc.model.AuthenticationResponse;
import com.rhbgroup.dte.obc.model.AuthenticationResponseAllOfData;
import com.rhbgroup.dte.obc.model.BakongAccountStatus;
import com.rhbgroup.dte.obc.model.BakongKYCStatus;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponseAcct;
import com.rhbgroup.dte.obc.model.CasaAccountStatus;
import com.rhbgroup.dte.obc.model.CasaKYCStatus;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponse;
import com.rhbgroup.dte.obc.model.FinishLinkAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponseAllOfData;
import com.rhbgroup.dte.obc.model.GetAccountDetailResponseAllOfDataLimit;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import com.rhbgroup.dte.obc.model.InitAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.UserModel;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AccountMapper {

  @Mapping(source = "bakongAccId", target = "bakongId")
  @Mapping(source = "login", target = "user.username")
  @Mapping(source = "key", target = "user.password")
  @Mapping(source = "loginType", target = "user.loginType")
  @Mapping(source = "phoneNumber", target = "user.mobileNo")
  AccountModel toModel(InitAccountRequest request);

  default InitAccountResponse toInitAccountResponse(
      UserModel userModel, String requestPhone, String jwtToken, boolean otpEnabled) {

    InitAccountResponseAllOfData data =
        new InitAccountResponseAllOfData().accessToken(jwtToken).requireOtp(otpEnabled);

    if (!requestPhone.equals(userModel.getMobileNo())) {
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
    entity.setAccountType(accountDetail.getAccountType());
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
  @Mapping(source = "ctryCitizen", target = "country")
  @Mapping(source = "currentBal", target = "balance")
  @Mapping(source = "currencyCode", target = "accCcy")
  @Mapping(source = "accountStatus", target = "accStatus", ignore = true)
  @Mapping(source = "kycStatus", target = "kycStatus", ignore = true)
  GetAccountDetailResponseAllOfData toAccountDetailData(CDRBGetAccountDetailResponseAcct response);

  default GetAccountDetailResponse mappingMobileNoAndAccStatus(
      String mobileNo, Double trxMin, Double trxMax, GetAccountDetailResponse response) {

    GetAccountDetailResponseAllOfData responseData = response.getData();

    responseData.setAccPhone(mobileNo);
    responseData.setLimit(
        new GetAccountDetailResponseAllOfDataLimit().maxTrxAmount(trxMax).minTrxAmount(trxMin));

    return response;
  }

  default GetAccountDetailResponse toAccountDetailResponse(
      CDRBGetAccountDetailResponseAcct response) {

    GetAccountDetailResponse mappingData =
        new GetAccountDetailResponse()
            .status(ResponseHandler.ok())
            .data(toAccountDetailData(response));

    if (CasaAccountStatus._1.equals(response.getAccountStatus())
        || CasaAccountStatus._4.equals(response.getAccountStatus())
        || CasaAccountStatus._5.equals(response.getAccountStatus())) {
      mappingData.getData().setAccStatus(BakongAccountStatus.ACTIVE);

    } else if (CasaAccountStatus._7.equals(response.getAccountStatus())
        || CasaAccountStatus._9.equals(response.getAccountStatus())) {
      mappingData.getData().setAccStatus(BakongAccountStatus.BLOCKED);

    } else {
      mappingData.getData().setAccStatus(BakongAccountStatus.CLOSED);
    }

    if (CasaKYCStatus.F.equals(response.getKycStatus())) {
      mappingData.getData().setKycStatus(BakongKYCStatus.FULL);

    } else if (CasaKYCStatus.V.equals(response.getKycStatus())
        || CasaKYCStatus.X.equals(response.getKycStatus())) {
      mappingData.getData().setKycStatus(BakongKYCStatus.PARTIAL);

    } else {
      mappingData.getData().setKycStatus(BakongKYCStatus.BASIC);
    }

    String acctType = "";
    if (response.getAccountType().getValue().equalsIgnoreCase("D")) {
      acctType = AppConstants.Account.CURRENT;
    } else if (response.getAccountType().getValue().equalsIgnoreCase("S")) {
      acctType = AppConstants.Account.SAVINGS;
    }
    mappingData.getData().setAccType(acctType);

    return mappingData;
  }

  @Mapping(source = "accountId", target = "accountNo")
  @Mapping(source = "accountStatus", target = "accountStatus", qualifiedByName = "toAccountStatus")
  AccountModel entityToModel(AccountEntity entity);

  @Named("toAccountStatus")
  default CasaAccountStatus toAccountStatus(String status) {
    return CasaAccountStatus.fromValue(status);
  }
}
