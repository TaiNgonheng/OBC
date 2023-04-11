package com.rhbgroup.dte.obc.domains.scheduler.controller;

import com.rhbgroup.dte.obc.api.SchedulerApiDelegate;
import com.rhbgroup.dte.obc.domains.scheduler.service.SchedulerJobService;
import com.rhbgroup.dte.obc.model.JobRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SchedulerController implements SchedulerApiDelegate {

  private final SchedulerJobService schedulerJobService;

  @Override
  public ResponseEntity<Void> createSchedulerJob(JobRequest jobRequest) {
    log.info("Create new scheduler job");
    schedulerJobService.createSchedulerJob(jobRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Override
  public ResponseEntity<Void> updateSchedulerJob(JobRequest jobRequest) {
    log.info("Update existing scheduler job");
    schedulerJobService.updateSchedulerJob(jobRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
