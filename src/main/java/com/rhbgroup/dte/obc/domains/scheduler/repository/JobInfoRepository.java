package com.rhbgroup.dte.obc.domains.scheduler.repository;

import com.rhbgroup.dte.obc.domains.scheduler.repository.entity.JobInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobInfoRepository extends JpaRepository<JobInfo, String> {}
