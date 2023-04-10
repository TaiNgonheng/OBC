package com.rhbgroup.dte.obc.domains.scheduler.mapper;

import com.rhbgroup.dte.obc.domains.scheduler.repository.entity.JobInfo;
import com.rhbgroup.dte.obc.model.JobRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SchedulerMapper {

  JobInfo toJobInfo(JobRequest request);
}
