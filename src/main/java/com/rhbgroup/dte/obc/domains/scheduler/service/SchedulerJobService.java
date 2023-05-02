package com.rhbgroup.dte.obc.domains.scheduler.service;

import com.rhbgroup.dte.obc.model.JobRequest;

public interface SchedulerJobService {

  void createSchedulerJob(JobRequest request);

  void updateSchedulerJob(JobRequest request);
}
