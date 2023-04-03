package com.rhbgroup.dte.obc.domains.account.repository;

import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

  Optional<AccountEntity> findByUserId(Long userId);
}
