package com.rhbgroup.dte.obc.domains.account.repository;

import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

  List<AccountEntity> findByUserIdAndLinkedStatus(Long userId, LinkedStatusEnum linkedStatusEnum);

  Optional<AccountEntity> findByUserIdAndBakongIdAndLinkedStatus(
      Long userId, String bakongId, LinkedStatusEnum linkedStatus);

  Optional<AccountEntity> findFirstByUserIdAndBakongIdAndLinkedStatus(
      Long userId, String bakongId, LinkedStatusEnum linkedStatus);

  Optional<AccountEntity> findByAccountIdAndLinkedStatus(
      String accountId, LinkedStatusEnum linkedStatus);

  Long countByAccountIdAndLinkedStatus(String accountId, LinkedStatusEnum linkedStatus);

  boolean existsByBakongIdAndAccountIdAndLinkedStatus(
      String bakongId, String accountId, LinkedStatusEnum COM);

  boolean existsByUserIdAndLinkedStatus(Long userId, LinkedStatusEnum COM);
}
