package com.rhbgroup.dte.obc.domains.transaction.repository;

import com.rhbgroup.dte.obc.domains.transaction.repository.entity.BatchReport;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchReportRepository extends JpaRepository<BatchReport, Long> {

  BatchReport findByDate(LocalDate date);
}
