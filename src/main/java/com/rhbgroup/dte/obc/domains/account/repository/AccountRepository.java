package com.rhbgroup.dte.obc.domains.account.repository;

import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

  Optional<AccountEntity> findByUserIdAndLinkedStatus(Long userId, String linkedStatus);

  Optional<AccountEntity> findByUserIdAndAccountId(Long userId, String accountId);

  Optional<AccountEntity> findByUserIdAndBakongIdAndLinkedStatus(
      Long userId, String bakongId, String linkedStatus);

  Optional<AccountEntity> findByAccountIdAndLinkedStatus(String accountId, String linkedStatus);
}
