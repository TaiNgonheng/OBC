package com.rhbgroup.dte.obc.domains.scheduler.job.impl;

import com.rhbgroup.dte.obc.domains.scheduler.job.JobFactory;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.model.JobNameEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
@AllArgsConstructor
public class JobFactoryImpl {

  private TransactionService transactionService;

  public JobFactory getItemFactory(JobNameEnum jobName) {
    if (JobNameEnum.TRXN_BATCH_PROCESSING.equals(jobName)) {
      return new BLCTrnxHistoryBatchJob(transactionService);
    }
    throw new ResourceAccessException("Item not found");
  }
}
