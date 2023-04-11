package com.rhbgroup.dte.obc.domains.transactions.service;

import com.rhbgroup.dte.obc.model.TransactionBatchFileProcessingRequest;

public interface TransactionService {

  void processTransactionHistoryBatchFile(TransactionBatchFileProcessingRequest request);
}
