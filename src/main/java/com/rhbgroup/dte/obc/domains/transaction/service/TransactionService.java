package com.rhbgroup.dte.obc.domains.transaction.service;

import com.rhbgroup.dte.obc.model.*;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsRequest;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponse;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.model.TransactionModel;

public interface TransactionService {

  void save(TransactionModel transactionModel);

  InitTransactionResponse initTransaction(InitTransactionRequest request);

  FinishTransactionResponse finishTransaction(FinishTransactionRequest request);

  GetAccountTransactionsResponse queryTransactionHistory(GetAccountTransactionsRequest request);

  void processTransactionHistoryBatchFile(TransactionBatchFileProcessingRequest request);
}
