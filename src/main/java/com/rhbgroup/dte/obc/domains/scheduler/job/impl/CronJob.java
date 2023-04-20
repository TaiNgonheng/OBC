package com.rhbgroup.dte.obc.domains.scheduler.job.impl;

import com.rhbgroup.dte.obc.domains.scheduler.job.JobFactory;
import com.rhbgroup.dte.obc.domains.scheduler.repository.JobItemRepository;
import com.rhbgroup.dte.obc.domains.scheduler.repository.entity.JobItem;
import com.rhbgroup.dte.obc.model.JobNameEnum;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@DisallowConcurrentExecution
public class CronJob extends QuartzJobBean {

  @Autowired private JobFactoryImpl jobFactory;

  @Autowired private JobItemRepository jobItemRepository;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    log.info("Context - {}", context);
    JobKey jobKey = context.getJobDetail().getKey();
    JobFactory job = jobFactory.getItemFactory(JobNameEnum.fromValue(jobKey.getName()));
    JobItem jobItem =
        JobItem.builder()
            .jobName(jobKey.getName())
            .jobGroup(jobKey.getGroup())
            .status(job.render())
            .triggerAt(LocalDateTime.now())
            .build();
    jobItemRepository.save(jobItem);
  }
}
