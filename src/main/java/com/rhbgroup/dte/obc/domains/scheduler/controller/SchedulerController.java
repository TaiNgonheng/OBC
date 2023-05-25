package com.rhbgroup.dte.obc.domains.scheduler.controller;

import com.rhbgroup.dte.obc.api.SchedulerApiDelegate;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.config.ApplicationProperties;
import com.rhbgroup.dte.obc.domains.scheduler.service.SchedulerJobService;
import com.rhbgroup.dte.obc.exceptions.BizException;
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
  private final ApplicationProperties properties;

  @Override
  public ResponseEntity<Void> createSchedulerJob(String allWatchToken, JobRequest jobRequest) {
    log.info("Create new scheduler job");
    if (!properties.getAllWatchToken().equals(allWatchToken)) {
      throw new BizException(ResponseMessage.BAD_REQUEST);
    }
    schedulerJobService.createSchedulerJob(jobRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Override
  public ResponseEntity<Void> updateSchedulerJob(String allWatchToken, JobRequest jobRequest) {
    log.info("Update existing scheduler job");
    if (!properties.getAllWatchToken().equals(allWatchToken)) {
      throw new BizException(ResponseMessage.BAD_REQUEST);
    }
    schedulerJobService.updateSchedulerJob(jobRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
