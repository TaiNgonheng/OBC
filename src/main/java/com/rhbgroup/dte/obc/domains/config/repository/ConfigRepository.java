package com.rhbgroup.dte.obc.domains.config.repository;

import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<ConfigEntity, Long> {
  Optional<ConfigEntity> getByServiceName(String serviceName);
}
