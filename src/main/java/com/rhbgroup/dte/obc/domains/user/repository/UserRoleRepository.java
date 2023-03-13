package com.rhbgroup.dte.obc.domains.user.repository;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserRoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Integer> {

  Optional<UserRoleEntity> findByUserId(String userId);
}
