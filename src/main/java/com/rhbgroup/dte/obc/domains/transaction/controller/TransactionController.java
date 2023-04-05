package com.rhbgroup.dte.obc.domains.transaction.controller;

import com.rhbgroup.dte.obc.api.TransactionApiDelegate;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionController implements TransactionApiDelegate {

  private final TransactionService transactionService;

  @Override
  public ResponseEntity<FinishTransactionResponse> finishTransaction(
      String authorization, FinishTransactionRequest finishTransactionRequest) {
    return Functions.of(transactionService::finishTransaction)
        .andThen(ResponseEntity::ok)
        .apply(authorization, finishTransactionRequest);
  }
}
