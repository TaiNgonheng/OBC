package com.rhbgroup.dte.obc.domains.transaction.service;

import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.TransactionModel;

public interface TransactionService {

  void save(TransactionModel transactionModel);

  FinishTransactionResponse finishTransaction(
      String authorization, FinishTransactionRequest finishTransactionRequest);
}
