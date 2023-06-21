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

  INVALID_LOGIN_TYPE(6, "Invalid login type."),

  INVALID_PHONE_NUMBER(6, "Invalid phone number. Please check the format and try again."),

  MISSING_PHONE_NUMBER(
      6,
      "phoneNumber is mandatory, phoneNumber length must be between 1 and 30, Invalid phone number. Please check the format and try again."),
  MISSING_OTP_CODE(6, "OTP is mandatory, OTP length must be 6"),
  MISSING_ACC_NUMBER(6, "accNumber is mandatory, accNumber length must be between 1 and 50"),
  MISSING_KEY(6, "key is mandatory, key length must be between 1 and 64"),
  OUT_OF_RANGE_LOGIN_TYPE(6, "loginType must be one of the following values: PHONE_PIN, USER_PWD"),

  INVALID_AMOUNT(6, "Invalid amount"),
  DESC_TOO_LONG(6, "desc length must be less than 30"),
  MISSING_TRANSFER_TYPE(
      6, "type is mandatory, type must in these values ['CASA_TO_WALLET', 'CASA_TO_CASA']"),
  INVALID_TRANSFER_TYPE(6, "Invalid transaction type."),
  INVALID_INITREFNUMBER(6, "initRefNumber length must be 32"),
  INVALID_TOKEN(7, "Token is invalid."),
  FAIL_TO_FETCH_ACCOUNT_DETAILS(8, "Getting user account information failed."),
  TRANSACTION_TO_UNAVAILABLE_ACCOUNT(9, "Transaction to unavailable account."),
  BALANCE_NOT_ENOUGH(10, "Not enough balance to do a transaction."),
  TRANSACTION_EXCEED_AMOUNT_LIMIT(
      11,
      "Transaction failed as the amount entered exceeds the allowed limit. Please enter a lower amount and try again or reach out to the merchant for further assistance."),
  DUPLICATE_SUBMISSION_ID(12, "Duplicate submissionId."),
  OTP_EXPIRED(17, "OTP expired. Please try again."),
  KYC_NOT_VERIFIED(14, "Cannot link account due to your account not yet verified."),
  ACCOUNT_ALREADY_LINKED(15, "This account is already linked to another Bakong account."),
  AUTHENTICATION_LOCKED(16, "Too many incorrect authentication attempts. Please try again later."),

  // CDRB error messages
  NO_SUCH_USER_FOUND(100, "User not found."),
  USER_ALREADY_EXIST(101, "User already exist."),
  FILE_PROCESSED(102, "The file input date has been processed"),
  BAD_REQUEST(103, "Bad Request"),

  // Generic error message
  CONSTRAINT_VIOLATION_ERROR(300, "Constraint violation exception."),
  DATA_NOT_FOUND(301, "Data not found."),
  DATA_STRUCTURE_INVALID(302, "Data structure invalid."),
  ;

  private final Integer code;
  private final String msg;
}
