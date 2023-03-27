package com.rhbgroup.dte.obc.domains.config.repository;

import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, Long> {

  Optional<ConfigEntity> getByConfigKey(String serviceName);

  List<ConfigEntity> findByConfigKeyIgnoreCaseStartingWith(String servicePrefix);

  List<ConfigEntity> findByConfigKeyIgnoreCaseContaining(String servicePrefix);
}
