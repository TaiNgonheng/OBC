package com.rhbgroup.dte.obc.domains.user.repository;

import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
  Optional<UserProfileEntity> getByUsername(String username);

  default void save(UserProfileEntity userProfile, String updateBy) {
    userProfile.setUpdatedBy(updateBy);
    userProfile.setUpdatedDate(Instant.now());
    save(userProfile);
  }
}
