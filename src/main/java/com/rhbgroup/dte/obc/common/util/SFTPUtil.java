package com.rhbgroup.dte.obc.common.util;

import com.rhbgroup.dte.obc.common.config.ApplicationProperties;
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

  private final ApplicationProperties applicationProperties;

  public byte[] downloadFileFromSFTP(String filename) throws IOException, URISyntaxException {
    FileSystemManager manager = VFS.getManager();
    String sftpCredentials =
        String.format(
            "%s:%s",
            applicationProperties.getSftpUsername(), applicationProperties.getSftpPassword());
    String path = String.format("/%s/%s", applicationProperties.getSftpPath(), filename);
    URI sftpUri =
        new URI(
            "sftp",
            sftpCredentials,
            applicationProperties.getSftpHost(),
            applicationProperties.getSftpPort(),
            path,
            null,
            null);

    URI maskedUri =
        new URI(
            "sftp",
            "***:***",
            applicationProperties.getSftpHost(),
            applicationProperties.getSftpPort(),
            path,
            null,
            null);
    log.debug("Downloading file with name '{}' from sftp url '{}'", filename, maskedUri);

    try (FileObject remote = manager.resolveFile(sftpUri)) {
      return remote.getContent().getByteArray();
    }
  }
}
