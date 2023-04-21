package com.rhbgroup.dte.obc.domains.scheduler.repository;

import com.rhbgroup.dte.obc.domains.scheduler.repository.entity.JobItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobItemRepository extends JpaRepository<JobItem, Long> {}
