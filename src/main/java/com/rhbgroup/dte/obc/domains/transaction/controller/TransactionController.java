package com.rhbgroup.dte.obc.domains.transaction.controller;

import com.rhbgroup.dte.obc.api.TransactionApiDelegate;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.config.ApplicationProperties;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.*;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsRequest;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponse;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
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
  private final ApplicationProperties properties;

  @Override
  public ResponseEntity<InitTransactionResponse> initTransaction(
      InitTransactionRequest initTransactionRequest) {

    return Functions.of(transactionService::initTransaction)
        .andThen(ResponseEntity::ok)
        .apply(initTransactionRequest);
  }

  @Override
  public ResponseEntity<Void> processTransactionHistoryBatchFile(
      String allWatchToken, TransactionBatchFileProcessingRequest request) {
    log.info("Start Api - process transaction history batch file");
    if (!properties.getAllWatchToken().equals(allWatchToken)) {
      throw new BizException(ResponseMessage.BAD_REQUEST);
    }
    CompletableFuture.runAsync(
        () -> transactionService.processTransactionHistoryBatchFile(request));
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Override
  public ResponseEntity<FinishTransactionResponse> finishTransaction(
      FinishTransactionRequest finishTransactionRequest) {

    return Functions.of(transactionService::finishTransaction)
        .andThen(ResponseEntity::ok)
        .apply(finishTransactionRequest);
  }

  @Override
  public ResponseEntity<GetAccountTransactionsResponse> queryTransactionHistory(
      GetAccountTransactionsRequest getAccountTransactionsRequest) {

    return Functions.of(transactionService::queryTransactionHistory)
        .andThen(ResponseEntity::ok)
        .apply(getAccountTransactionsRequest);
  }
}
