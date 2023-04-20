package com.rhbgroup.dte.obc.domains.scheduler.job.impl;

import com.rhbgroup.dte.obc.domains.scheduler.job.JobFactory;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.model.TransactionBatchFileProcessingRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class BLCTrnxHistoryBatchJob implements JobFactory {

  private final TransactionService transactionService;

  @Override
  public boolean render() {
    try {
      transactionService.processTransactionHistoryBatchFile(
          new TransactionBatchFileProcessingRequest());
    } catch (Exception e) {
      log.error("Something went wrong with bakong link casa transaction history batch file", e);
      return false;
    }
    return true;
  }
}
