package com.rhbgroup.dte.obc.domains.transactions.service.impl;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.rhbgroup.dte.obc.common.util.EncryptDecryptUtil;
import com.rhbgroup.dte.obc.common.util.SFTPUtil;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.transactions.mapper.TransactionMapper;
import com.rhbgroup.dte.obc.domains.transactions.model.SIBSBatchTransaction;
import com.rhbgroup.dte.obc.domains.transactions.repository.SIBSTransactionRepository;
import com.rhbgroup.dte.obc.domains.transactions.repository.entity.SIBSTransaction;
import com.rhbgroup.dte.obc.domains.transactions.service.TransactionService;
import com.rhbgroup.dte.obc.model.PGPConfig;
import com.rhbgroup.dte.obc.model.SIBSSyncDateConfig;
import java.io.*;
import java.security.Security;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

  private final ConfigService configService;
  private final SFTPUtil sftpUtil;
  private final EncryptDecryptUtil encryptDecryptUtil;
  private final TransactionMapper transactionMapper;
  private final SIBSTransactionRepository sibsTransactionRepository;

  private static final String TRANSACTION_FILE_PREFIX = "OBCDailyTrx_";
  private static final String SIBS_SYNC_DATE_KEY = "SIBS_DATE_CONFIG";
  private static final String PGP_KEY = "PGP_CONFIG";
  private static final String DDMMYYYY = "ddMMyyyy";
  private static final String TRANSACTION_FILE_EXTENSION = ".csv.pgp";

  @Override
  public void processTransactionHistoryBatchFile() {
    // if provider is not present, add it
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      // insert at specific position
      Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    PGPConfig pgpConfig = configService.getByConfigKey(PGP_KEY, PGPConfig.class);

    // Download batch file from sftp
    InputStream encryptedFile = null;

    try {
      String filename = generateTransactionHistoryFilename();
      encryptedFile = new ByteArrayInputStream(sftpUtil.downloadFileFromSFTP(filename));
    } catch (Exception e) {
      log.error("Couldn't download file from sftp file", e);
    }

    InputStream pgpKeyIs = getLocalFile(pgpConfig.getPgpKeyPath());

    if (encryptedFile != null && pgpKeyIs != null) {
      InputStream decryptedFile = null;
      try {
        decryptedFile =
            encryptDecryptUtil.decryptFile(encryptedFile, pgpConfig.getPgpPassphrase(), pgpKeyIs);
      } catch (Exception e) {
        log.error("Couldn't decrypted transaction history file", e);
      }

      if (decryptedFile != null) {
        log.debug(
            "Successfully downloaded and decrypted transaction history file. The file processing will start.");
        parseFileAndStoreRecordToDB(decryptedFile);
      }
    }
  }

  private String generateTransactionHistoryFilename() {
    SIBSSyncDateConfig sibsSyncDateConfig =
        configService.getByConfigKey(SIBS_SYNC_DATE_KEY, SIBSSyncDateConfig.class);
    LocalDate fileDate = LocalDate.now().minusDays(1);
    if (Boolean.TRUE.equals(sibsSyncDateConfig.getUseSIBSSyncDate())) {
      fileDate =
          LocalDate.parse(sibsSyncDateConfig.getSibsSyncDate(), DateTimeFormatter.ISO_DATE)
              .minusDays(1);
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DDMMYYYY);
    return TRANSACTION_FILE_PREFIX + formatter.format(fileDate) + TRANSACTION_FILE_EXTENSION;
  }

  private InputStream getLocalFile(String keyPath) {
    try {
      return new FileInputStream(keyPath);
    } catch (IOException e) {
      log.error("PGP key file couldn't be found in {}", keyPath, e);
    }
    return null;
  }

  private void parseFileAndStoreRecordToDB(InputStream is) {
    CsvMapper csvMapper = new CsvMapper();
    CsvSchema csvSchema = csvMapper.typedSchemaFor(SIBSBatchTransaction.class).withHeader();
    try {
      MappingIterator<SIBSBatchTransaction> transactionIterator =
          new CsvMapper()
              .readerFor(SIBSBatchTransaction.class)
              .with(csvSchema.withColumnSeparator(','))
              .readValues(is);
      List<SIBSBatchTransaction> batchTransactions = transactionIterator.readAll();
      List<SIBSTransaction> transactions = transactionMapper.toSIBSTransactions(batchTransactions);
      sibsTransactionRepository.saveAll(transactions);
    } catch (IOException e) {
      log.error("Cannot mapped csv file");
      e.printStackTrace();
    } catch (Exception e) {
      log.error("Something went wrong while processing the dormant file", e);
    }
  }
}
