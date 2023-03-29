package com.rhbgroup.dte.obc.common.util;

import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.model.SFTPConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class SFTPUtil {

  private final ConfigService configService;

  private static final String SFTP_ACCOUNT_KEY = "SFTP_CONFIG";

  public byte[] downloadFileFromSFTP(String filename) throws IOException, URISyntaxException {
    FileSystemManager manager = VFS.getManager();

    SFTPConfig config = configService.getByConfigKey(SFTP_ACCOUNT_KEY, SFTPConfig.class);
    String sftpCredentials = String.format("%s:%s", config.getUsername(), config.getPassword());
    String path = String.format("/%s/%s", config.getRemotePath(), filename);
    URI sftpUri =
        new URI(
            "sftp",
            sftpCredentials,
            config.getRemoteHost(),
            config.getRemotePort(),
            path,
            null,
            null);

    URI maskedUri =
        new URI(
            "sftp", "***:***", config.getRemoteHost(), config.getRemotePort(), path, null, null);
    log.debug("Downloading file with name '{}' from sftp url '{}'", filename, maskedUri);

    FileObject remote = manager.resolveFile(sftpUri);
    byte[] fileBytes = remote.getContent().getByteArray();
    remote.close();
    return fileBytes;
  }
}
