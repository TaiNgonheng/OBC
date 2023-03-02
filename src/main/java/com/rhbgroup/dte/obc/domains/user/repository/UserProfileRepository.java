package com.rhbgroup.dte.obc.domains.user.repository;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

  Optional<UserProfileEntity> getByUsername(String username);

}
