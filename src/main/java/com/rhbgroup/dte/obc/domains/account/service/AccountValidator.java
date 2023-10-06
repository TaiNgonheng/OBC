package com.rhbgroup.dte.obc.domains.account.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.enums.AccountStatusEnum;
import com.rhbgroup.dte.obc.common.enums.KycStatusEnum;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.CustomBizException;
import com.rhbgroup.dte.obc.model.*;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;

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

  public static void validateBalanceAndCurrency(
      CDRBGetAccountDetailResponse account,
      InitTransactionRequest request,
      CDRBFeeAndCashbackResponse feeAndCashback) {

    if (!request.getCcy().equalsIgnoreCase(account.getAcct().getCurrencyCode())) {
      throw new BizException(ResponseMessage.INVALID_CURRENCY);
    }

    if (request.getAmount() > account.getAcct().getCurrentBal()) {
      throw new BizException(ResponseMessage.BALANCE_NOT_ENOUGH);
    }

    if (request.getAmount() + feeAndCashback.getFee() > account.getAcct().getCurrentBal()) {
      throw new BizException(ResponseMessage.BALANCE_NOT_ENOUGH_INCLUDE_FEE);
    }
  }

  public static void validateKYCStatus(CDRBGetAccountDetailResponse account) {
    // Check if CASA account is not Fully KYC
    CasaKYCStatus kycStatus = account.getAcct().getKycStatus();
    if (ObjectUtils.isEmpty(kycStatus) || !kycStatus.equals(CasaKYCStatus.F)) {
      throw new BizException(ResponseMessage.KYC_NOT_VERIFIED);
    }
  }

  public static void validateAccountStatus(CDRBGetAccountDetailResponse account) {
    CasaAccountStatus status = account.getAcct().getAccountStatus();
    if (!status.equals(CasaAccountStatus._1) && !status.equals(CasaAccountStatus._4)) {
      throw new CustomBizException(
          9,
          "An error was encountered when processing account with status "
              + casaAccountStatusMap.get(status.getValue()));
    }
  }

  public static void validateAccountAndKYCStatus(CDRBGetAccountDetailResponse account) {
    validateAccountStatus(account);
    validateKYCStatus(account);
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
