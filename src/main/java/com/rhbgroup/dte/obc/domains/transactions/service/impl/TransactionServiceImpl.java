package com.rhbgroup.dte.obc.domains.transactions.service.impl;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.rhbgroup.dte.obc.common.util.SFTPUtil;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.domains.transactions.mapper.TransactionMapper;
import com.rhbgroup.dte.obc.domains.transactions.model.SIBSBatchTransaction;
import com.rhbgroup.dte.obc.domains.transactions.repository.BatchReportRepository;
import com.rhbgroup.dte.obc.domains.transactions.repository.SIBSTransactionRepository;
import com.rhbgroup.dte.obc.domains.transactions.repository.entity.BatchReport;
import com.rhbgroup.dte.obc.domains.transactions.repository.entity.SIBSTransaction;
import com.rhbgroup.dte.obc.domains.transactions.service.TransactionService;
import com.rhbgroup.dte.obc.model.BatchReportStatus;
import com.rhbgroup.dte.obc.model.SIBSSyncDateConfig;
import com.rhbgroup.dte.obc.model.TransactionBatchFileProcessingRequest;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

  private final ConfigService configService;
  private final SFTPUtil sftpUtil;
  private final TransactionMapper transactionMapper;
  private final SIBSTransactionRepository sibsTransactionRepository;
  private final BatchReportRepository batchReportRepository;

  private static final String TRANSACTION_FILE_PREFIX = "OBCDailyTrx_";
  private static final String SIBS_SYNC_DATE_KEY = "SIBS_SYNC_DATE_CONFIG";
  private static final String DDMMYYYY = "ddMMyyyy";
  private static final String TRANSACTION_FILE_EXTENSION = ".csv";
  private static final int MAX_STACK_TRACE_LENGTH = 19999;

  @Override
  public void processTransactionHistoryBatchFile(TransactionBatchFileProcessingRequest request) {
    LocalDate date = getProcessingDate(request);
    BatchReport report = batchReportRepository.findByDate(date);
    if (ObjectUtils.isNotEmpty(report)
        && !report.getStatus().equals(BatchReportStatus.FAILED)) {
      throw new IllegalArgumentException("The file input date has been processed");
    }
    report.setStatus(BatchReportStatus.PENDING);
    batchReportRepository.saveAndFlush(report);
    try {
      String filename = generateTransactionHistoryFilename(date);
      InputStream batchFile = new ByteArrayInputStream(sftpUtil.downloadFileFromSFTP(filename));
      parseFileAndStoreRecordToDB(batchFile);
      batchFile.close();
      report.setErrorMessage(null);
      report.setStatus(BatchReportStatus.COMPLETED);
      batchReportRepository.saveAndFlush(report);
    } catch (Exception e) {
      String stackTrace = ExceptionUtils.getStackTrace(e);
      if (StringUtils.isNotBlank(stackTrace) && stackTrace.length() > MAX_STACK_TRACE_LENGTH) {
        stackTrace = stackTrace.substring(0, MAX_STACK_TRACE_LENGTH);
      }
      report.setStatus(BatchReportStatus.FAILED);
      report.setErrorMessage(stackTrace);
      batchReportRepository.saveAndFlush(report);
      log.error("Error happen when processing the batch file", e);
    }
  }

  private LocalDate getProcessingDate(TransactionBatchFileProcessingRequest request) {
    LocalDate date = request.getDate();
    if (request.getDate() == null) {
      SIBSSyncDateConfig sibsSyncDateConfig =
          configService.getByConfigKey(SIBS_SYNC_DATE_KEY, SIBSSyncDateConfig.class);
      date = LocalDate.now().minusDays(1);
      if (Boolean.TRUE.equals(sibsSyncDateConfig.getUseSIBSSyncDate())) {
        date =
            LocalDate.parse(sibsSyncDateConfig.getSibsSyncDate(), DateTimeFormatter.ISO_DATE)
                .minusDays(1);
      }
    }
    return date;
  }

  private String generateTransactionHistoryFilename(LocalDate date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DDMMYYYY);
    return TRANSACTION_FILE_PREFIX + formatter.format(date) + TRANSACTION_FILE_EXTENSION;
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
