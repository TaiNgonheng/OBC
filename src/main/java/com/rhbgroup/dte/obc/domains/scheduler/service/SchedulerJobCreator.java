package com.rhbgroup.dte.obc.domains.scheduler.service;

import java.util.Date;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public interface SchedulerJobCreator {
  JobDetail createJob(
      Class<? extends QuartzJobBean> jobClass,
      boolean isDurable,
      ApplicationContext context,
      String jobName,
      String jobGroup,
      String description);

  CronTrigger createCronTrigger(
      String triggerName, Date startTime, String cronExpression, int misFireInstruction);
}
