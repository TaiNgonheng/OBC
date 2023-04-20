package com.rhbgroup.dte.obc.domains.transaction.controller;

import com.rhbgroup.dte.obc.api.TransactionApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.model.*;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionController implements TransactionApiDelegate {

  private final TransactionService transactionService;

  @Override
  public ResponseEntity<InitTransactionResponse> initTransaction(
      InitTransactionRequest initTransactionRequest) {
    return Functions.of(transactionService::initTransaction)
        .andThen(ResponseEntity::ok)
        .apply(initTransactionRequest);
  }

  @Override
  public ResponseEntity<Void> processTransactionHistoryBatchFile(
      TransactionBatchFileProcessingRequest request) {
    log.info("Start Api - process transaction history batch file");
    CompletableFuture.runAsync(
        () -> transactionService.processTransactionHistoryBatchFile(request));
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Override
  public ResponseEntity<FinishTransactionResponse> finishTransaction(
      String authorization, FinishTransactionRequest finishTransactionRequest) {
    return Functions.of(transactionService::finishTransaction)
        .andThen(ResponseEntity::ok)
        .apply(authorization, finishTransactionRequest);
  }
}
