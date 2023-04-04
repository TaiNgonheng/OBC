package com.rhbgroup.dte.obc.domains.account.repository;

import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

  Optional<AccountEntity> findFirstByUserId(Long userId);

  Optional<AccountEntity> findByUserIdAndBakongIdAndLinkedStatus(
      Long userId, String bakongId, LinkedStatusEnum linkedStatus);

  Optional<AccountEntity> findFirstByUserIdAndBakongIdAndLinkedStatus(
      Long userId, String bakongId, LinkedStatusEnum linkedStatus);

  Optional<AccountEntity> findByAccountIdAndLinkedStatus(
      String accountId, LinkedStatusEnum linkedStatus);
}
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
  AccountEntity getByAccountIdAndUserId(String accountId, Long userId);
}
