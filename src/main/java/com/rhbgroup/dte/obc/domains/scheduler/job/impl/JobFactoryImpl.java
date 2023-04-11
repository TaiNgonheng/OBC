package com.rhbgroup.dte.obc.domains.scheduler.job.impl;

import com.rhbgroup.dte.obc.domains.scheduler.job.JobFactory;
import com.rhbgroup.dte.obc.domains.transactions.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
@AllArgsConstructor
public class JobFactoryImpl {

  private TransactionService transactionService;

  public JobFactory getItemFactory(String type) {
    if ("TRXN_BATCH_PROCESSING".equals(type)) {
      return new BLCTrnxHistoryBatchJob(transactionService);
    }
    throw new ResourceAccessException("Item not found");
  }
}
