package com.rhbgroup.dte.obc.domains.transactions.repository;

import com.rhbgroup.dte.obc.domains.transactions.repository.entity.BatchReport;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchReportRepository extends JpaRepository<BatchReport, Long> {

  BatchReport findByDate(LocalDate date);
}
