package com.rhbgroup.dte.obc.domains.scheduler.repository.entity;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_job_item")
public class JobItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "job_name")
  private String jobName;

  @Column(name = "job_group")
  private String jobGroup;

  @Column(name = "status")
  private Boolean status;

  @Column(name = "trigger_at")
  private LocalDateTime triggerAt;
}
