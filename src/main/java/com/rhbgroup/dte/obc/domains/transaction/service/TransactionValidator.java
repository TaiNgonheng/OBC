package com.rhbgroup.dte.obc.domains.transaction.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;

public class TransactionValidator {

  private TransactionValidator() {}

  public static void validateInitTransaction(
      InitTransactionRequest request,
      ConfigService transactionConfig,
      AccountEntity accountEntity) {

    Double minAmt =
        transactionConfig.getValue(ConfigConstants.Transaction.MIN_AMOUNT, Double.class);
    Double maxAmt =
        transactionConfig.getValue(ConfigConstants.Transaction.MAX_AMOUNT, Double.class);

    if (!accountEntity.getAccountId().equals(request.getSourceAcc())) {
      throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
    }

    if (request.getAmount() < minAmt || request.getAmount() > maxAmt) {
      throw new BizException(ResponseMessage.TRANSACTION_EXCEED_AMOUNT_LIMIT);
    }

    if (!request.getCcy().equalsIgnoreCase(accountEntity.getAccountCcy())) {
      // need one more error code and error msg
      throw new BizException(ResponseMessage.INTERNAL_SERVER_ERROR);
    }
  }
}
