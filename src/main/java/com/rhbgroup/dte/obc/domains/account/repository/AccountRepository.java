package com.rhbgroup.dte.obc.domains.account.repository;

import com.rhbgroup.dte.obc.common.enums.LinkedStatusEnum;
import com.rhbgroup.dte.obc.domains.account.repository.entity.AccountEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  void getFindByUserId(Long userId);

  List<AccountEntity> findByUserId(Long userId);

  @Query(
      value =
          "SELECT a FROM tbl_obc_account a WHERE a.userId = :userId AND a.bakongId <> :excludeBakongId AND a.linkedStatus = :linkedStatus",
      nativeQuery = true)
  List<AccountEntity> findByUserIdAndNotTheBakongIdAndLinkedStatus(
      @Param("userId") Long userId,
      @Param("excludeBakongId") String excludeBakongId,
      @Param("linkedStatus") LinkedStatusEnum linkedStatus);
}
