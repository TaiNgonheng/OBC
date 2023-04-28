package com.rhbgroup.dte.obc.runner;

import com.rhbgroup.dte.obc.domains.user.repository.UserProfileRepository;
import com.rhbgroup.dte.obc.domains.user.repository.entity.UserProfileEntity;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GowaveUserGenerator implements ApplicationRunner {

  @Value("${obc.gowave-user}")
  private String gowaveUser;

  @Value("${obc.gowave-password}")
  private String gowavePassword;

  private final PasswordEncoder passwordEncoder;
  private final UserProfileRepository userProfileRepository;

  @Override
  public void run(ApplicationArguments args) {
    Optional<UserProfileEntity> userOptional = userProfileRepository.getByUsername(gowaveUser);
    if (userOptional.isEmpty()) {
      UserProfileEntity entity = new UserProfileEntity();
      entity.setUsername(gowaveUser);
      entity.setPassword(passwordEncoder.encode(gowavePassword));
      entity.setDeleted(false);
      entity.setOtpVerifiedStatus(true);
      entity.setOtpVerifiedDate(Instant.now());

      userProfileRepository.save(entity);
      log.info("Created new  gowave user");
    }
  }
}
