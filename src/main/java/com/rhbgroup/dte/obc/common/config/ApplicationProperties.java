package com.rhbgroup.dte.obc.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ApplicationProperties {

  @Value("${obc.sftp.username}")
  private String sftpUsername;

  @Value("${obc.sftp.password}")
  private String sftpPassword;

  @Value("${obc.sftp.host}")
  private String sftpHost;

  @Value("${obc.sftp.port}")
  private Integer sftpPort;

  @Value("${obc.sftp.path}")
  private String sftpPath;
}
