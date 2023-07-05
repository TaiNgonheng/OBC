package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.DynamicBizException;
import com.rhbgroup.dte.obc.model.CDRBGetAccountDetailResponse;
import com.rhbgroup.dte.obc.model.CasaAccountStatus;
import com.rhbgroup.dte.obc.model.CasaKYCStatus;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.PGProfileResponse;
import java.util.Map;

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
    CasaKYCStatus kycStatus = account.getAcct().getKycStatus();
    if (!kycStatus.equals(CasaKYCStatus.F)) {
      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
    }

    // 1 = Active, 2 = Closed, 4 = New Today, 5 = Do not close on Zero, 7 = Frozen, 9 = Dormant
    CasaAccountStatus accountStatus = account.getAcct().getAccountStatus();
    if (accountStatus.equals(CasaAccountStatus._2)
        || accountStatus.equals(CasaAccountStatus._7)
        || accountStatus.equals(CasaAccountStatus._9)) {

      throw new BizException(ResponseMessage.ACCOUNT_DEACTIVATED);
    }
  }

  public static void validateBalanceAndCurrency(
      CDRBGetAccountDetailResponse account, InitTransactionRequest request) {

    if (!request.getCcy().equalsIgnoreCase(account.getAcct().getCurrencyCode())) {
      throw new BizException(ResponseMessage.INVALID_CURRENCY);
    }

    // Do we need to include fee + transaction amount?
    if (request.getAmount() > account.getAcct().getCurrentBal()) {
      throw new BizException(ResponseMessage.BALANCE_NOT_ENOUGH);
    }
  }

  public static void validateAccountStatus(CDRBGetAccountDetailResponse account) {
    CasaAccountStatus status = account.getAcct().getAccountStatus();
    if (!status.equals(CasaAccountStatus._1) && !status.equals(CasaAccountStatus._4)) {
      throw new DynamicBizException(
          9,
          "An error was encountered when processing account with status "
              + casaAccountStatusMap.get(status.getValue()));
    }
  }

  private static final Map<String, String> casaAccountStatusMap =
      Map.of(
          "1", "ACTIVE",
          "2", "CLOSED",
          "3", "MATURED NOT REDEEM",
          "4", "NEW TODAY",
          "5", "DO NOT CLOSE ON ZERO",
          "6", "No debit allowed",
          "7", "FROZEN",
          "8", "CHARGE OFF",
          "9", "DORMANT");
}
