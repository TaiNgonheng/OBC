package com.rhbgroup.dte.obc.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResponseMessage {

  // NBC error messages
  INTERNAL_SERVER_ERROR(1, "Internal server error."),
  ACCOUNT_DEACTIVATED(2, "Account is deactivated."),
  NO_ACCOUNT_FOUND(3, "No account found."),
  AUTHENTICATION_FAILED(4, "Authentication error, please try again."),
  SESSION_EXPIRED(5, "Your Session has expired."),
  MANDATORY_FIELD_MISSING(6, "Missing mandatory element."),
  INVALID_TOKEN(7, "Token is invalid."),
  FAIL_TO_FETCH_ACCOUNT_DETAILS(8, "Getting user account information failed."),
  TRANSACTION_TO_UNAVAILABLE_ACCOUNT(9, "Transaction to unavailable account."),
  BALANCE_NOT_ENOUGH(10, "Not enough balance to do a transaction."),
  TRANSACTION_EXCEED_AMOUNT_LIMIT(
      11,
      "Transaction failed as the amount entered exceeds the allowed limit. Please enter a lower amount and try again or reach out to the merchant for further assistance."),
  DUPLICATE_SUBMISSION_ID(12, "Duplicate submissionId."),
  OTP_EXPIRED(13, "OTP expired. Please try again."),
  KYC_NOT_VERIFIED(14, "Cannot link account due to your account not yet verified."),
  ACCOUNT_ALREADY_LINKED(15, "This account is already linked to another Bakong account."),
  AUTHENTICATION_LOCKED(16, "Too many incorrect authentication attempts. Please try again later."),

  // CDRB error messages
  NO_SUCH_USER_FOUND(100, "User not found."),
  USER_ALREADY_EXIST(101, "User already exist."),
  FILE_PROCESSED(102, "The file input date has been processed"),

  // Generic error message
  CONSTRAINT_VIOLATION_ERROR(300, "Constraint violation exception."),
  DATA_NOT_FOUND(301, "Data not found."),
  DATA_STRUCTURE_INVALID(302, "Data structure invalid."),
  ;

  private final Integer code;
  private final String msg;
}
