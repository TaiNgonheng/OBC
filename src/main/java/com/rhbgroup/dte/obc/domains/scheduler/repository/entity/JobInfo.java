package com.rhbgroup.dte.obc.domains.scheduler.repository.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@Table(name = "scheduler_job_info")
public class JobInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String jobId;

  private String jobName;
  private String jobGroup;
  private String jobStatus;
  private String jobClass;
  private String cronExpression;
  private String desc;
  private String interfaceName;
}
