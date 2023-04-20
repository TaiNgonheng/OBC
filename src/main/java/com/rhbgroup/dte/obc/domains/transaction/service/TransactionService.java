package com.rhbgroup.dte.obc.domains.transaction.service;

import com.rhbgroup.dte.obc.model.*;

public interface TransactionService {

  void save(TransactionModel transactionModel);

  InitTransactionResponse initTransaction(InitTransactionRequest request);

  FinishTransactionResponse finishTransaction(
      String authorization, FinishTransactionRequest finishTransactionRequest);

  void processTransactionHistoryBatchFile(TransactionBatchFileProcessingRequest request);
}
