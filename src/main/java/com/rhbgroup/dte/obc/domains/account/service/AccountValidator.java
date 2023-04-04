package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponseAcct;
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

    // Check if CASA account is not Fully KYC
    CDRBGetAccountDetailResponseAcct.KycStatusEnum kycStatus = account.getAcct().getKycStatus();
    if (!kycStatus.equals(CDRBGetAccountDetailResponseAcct.KycStatusEnum.F)) {
      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
    }

    // 1 = Active, 2 = Closed, 4 = New Today, 5 = Do not close on Zero, 7 = Frozen, 9 = Dormant
    CDRBGetAccountDetailResponseAcct.AccountStatusEnum accountStatus =
        account.getAcct().getAccountStatus();
    if (accountStatus.equals(CDRBGetAccountDetailResponseAcct.AccountStatusEnum._2)
        || accountStatus.equals(CDRBGetAccountDetailResponseAcct.AccountStatusEnum._7)
        || accountStatus.equals(CDRBGetAccountDetailResponseAcct.AccountStatusEnum._9)) {

      throw new BizException(ResponseMessage.ACCOUNT_DEACTIVATED);
    }
  }
}
