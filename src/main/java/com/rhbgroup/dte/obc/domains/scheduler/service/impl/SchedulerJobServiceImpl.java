package com.rhbgroup.dte.obc.domains.scheduler.service.impl;

import com.rhbgroup.dte.obc.domains.scheduler.job.CronJob;
import com.rhbgroup.dte.obc.domains.scheduler.mapper.SchedulerMapper;
import com.rhbgroup.dte.obc.domains.scheduler.repository.JobInfoRepository;
import com.rhbgroup.dte.obc.domains.scheduler.service.SchedulerJobCreator;
import com.rhbgroup.dte.obc.domains.scheduler.service.SchedulerJobService;
import com.rhbgroup.dte.obc.model.JobRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class SchedulerJobServiceImpl implements SchedulerJobService {

  private final SchedulerFactoryBean schedulerFactoryBean;
  private final SchedulerJobCreator schedulerJobCreator;
  private final JobInfoRepository schedulerInfoRepository;
  private final ApplicationContext applicationContext;
  private final SchedulerMapper schedulerMapper;

  @Override
  public void createSchedulerJob(JobRequest request) {
    try {
      Scheduler scheduler = schedulerFactoryBean.getScheduler();
      JobDetail jobDetail =
          JobBuilder.newJob(CronJob.class)
              .withIdentity(request.getJobName(), request.getJobGroup())
              .build();
      if (scheduler.checkExists(jobDetail.getKey())) {
        log.debug("Job already exists");
      }
      jobDetail =
          schedulerJobCreator.createJob(
              CronJob.class,
              false,
              applicationContext,
              request.getJobName(),
              request.getJobGroup());
      Trigger trigger =
          schedulerJobCreator.createCronTrigger(
              request.getJobName(),
              new Date(),
              request.getCronExpression(),
              SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
      scheduler.scheduleJob(jobDetail, trigger);
      request.setJobStatus("SCHEDULED");
      schedulerInfoRepository.save(schedulerMapper.toJobInfo(request));
    } catch (Exception e) {
      log.error("Something went wrong", e);
    }
  }
}
