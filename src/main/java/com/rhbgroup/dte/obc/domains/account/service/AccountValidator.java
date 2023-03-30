package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.PGProfileResponse;

public class AccountValidator {

  private AccountValidator() {}

  public static void validateAccount(PGProfileResponse userProfile) {
    if (!KycStatusEnum.parse(userProfile.getKycStatus()).equals(KycStatusEnum.FULL_KYC)) {
      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
    }

    if (AccountStatusEnum.parse(userProfile.getAccountStatus())
        .equals(AccountStatusEnum.DEACTIVATED)) {
      throw new BizException(ResponseMessage.ACCOUNT_DEACTIVATED);
    }
  }

  public static void validateCasaAccount(CDRBGetAccountDetailResponse account) {
    if (!account.getKycVerified()) {
      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
    }
  }
}
