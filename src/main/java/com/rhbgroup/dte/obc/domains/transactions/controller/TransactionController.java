package com.rhbgroup.dte.obc.domains.transactions.controller;

import com.rhbgroup.dte.obc.api.TransactionApiDelegate;
import com.rhbgroup.dte.obc.domains.transactions.service.TransactionService;
import com.rhbgroup.dte.obc.model.TransactionBatchFileProcessingRequest;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionController implements TransactionApiDelegate {

  private final TransactionService transactionService;

  @Override
  public ResponseEntity<Void> processTransactionHistoryBatchFile(
      TransactionBatchFileProcessingRequest request) {
    log.info("Start Api - process transaction history batch file");
    CompletableFuture.runAsync(
        () -> transactionService.processTransactionHistoryBatchFile(request));
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
