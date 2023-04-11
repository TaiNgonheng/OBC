package com.rhbgroup.dte.obc.domains.transaction.service;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.ConfigConstants;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionEntity;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.TransactionStatus;
import com.rhbgroup.dte.obc.model.TransactionType;

public class TransactionValidator {

  private TransactionValidator() {}

  public static void validateInitTransaction(
      InitTransactionRequest request, ConfigService transactionConfig, AccountModel accountModel) {

    // Only support CASA_TO_WALLET for now
    if (TransactionType.CASA.equals(request.getType())) {
      throw new BizException(ResponseMessage.OPERATION_NOT_SUPPORTED);
    }

    Double minAmt =
        transactionConfig.getValue(ConfigConstants.Transaction.MIN_AMOUNT, Double.class);
    Double maxAmt =
        transactionConfig.getValue(ConfigConstants.Transaction.MAX_AMOUNT, Double.class);

    if (!accountModel.getAccountNo().equals(request.getSourceAcc())) {
      throw new BizException(ResponseMessage.NO_ACCOUNT_FOUND);
    }

    if (request.getAmount() < minAmt || request.getAmount() > maxAmt) {
      throw new BizException(ResponseMessage.TRANSACTION_EXCEED_AMOUNT_LIMIT);
    }

    if (!request.getCcy().equalsIgnoreCase(accountModel.getAccountCcy())) {
      throw new BizException(ResponseMessage.MANDATORY_FIELD_MISSING);
    }
  }

  public static void validateTransactionStatus(TransactionEntity transaction) {
    if (TransactionStatus.COMPLETED.equals(transaction.getTrxStatus())) {
      throw new BizException(ResponseMessage.DUPLICATE_SUBMISSION_ID);
    }
  }
}
