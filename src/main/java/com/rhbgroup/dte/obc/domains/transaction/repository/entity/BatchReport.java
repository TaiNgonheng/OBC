package com.rhbgroup.dte.obc.domains.transaction.repository.entity;

import com.rhbgroup.dte.obc.model.BatchReportStatus;
import java.time.LocalDate;
import javax.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_batch_reports")
public class BatchReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "date")
  private LocalDate date;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private BatchReportStatus status;

  @Column(name = "error_msg")
  private String errorMessage;
}
