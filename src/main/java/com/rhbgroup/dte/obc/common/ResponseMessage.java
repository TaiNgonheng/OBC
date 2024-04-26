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
  OUT_OF_RANGE_LOGIN_TYPE(6, "loginType must be one of the following values: PHONE_PIN, USER_PWD"),

  INVALID_AMOUNT(6, "Invalid amount"),
  DESC_TOO_LONG(6, "The description exceeds the maximum characters limit."),
  MISSING_LOGIN_TYPE(
      6,
      "loginType is mandatory, loginType must be one of the following values: PHONE_PIN, USER_PWD"),

  MISSING_LOGIN(6, "login is mandatory, login length must be between 1 and 30"),
  MISSING_PHONE_NUMBER(
      6,
      "phoneNumber is mandatory, phoneNumber length must be between 1 and 30, Invalid phone number. Please check the format and try again."),
  MISSING_TRANSFER_TYPE(
      6, "type is mandatory, type must in these values ['CASA_TO_WALLET', 'CASA_TO_CASA']"),

  MISSING_OTP_CODE(6, "OTP is mandatory, OTP length must be 6"),

  OTP_NOT_VERIFIED(6, "OTP step missed"),
  MISSING_ACC_NUMBER(6, "accNumber is mandatory, accNumber length must be between 1 and 50"),
  MISSING_KEY(6, "key is mandatory, key length must be between 1 and 64"),
  MISSING_SOURCE_ACC(6, "sourceAcc is mandatory, sourceAcc length must be between 1 and 64"),
  MISSING_BAKONG_ACC_ID(6, "bakongAccId is mandatory, bakongAccId length must be between 1 and 50"),
  MISSING_DESTINATION_ACC_ID(
      6, "destinationAcc is mandatory, destinationAcc length must be between 1 and 50"),

  MISSING_AMOUNT(6, "amount is mandatory"),

  MISSING_CCY(6, "ccy is mandatory"),

  MISSING_PAGE(6, "page is mandatory"),
  MISSING_PAGE_SIZE(6, "size is mandatory"),
  INVALID_TRANSFER_TYPE(6, "Invalid transaction type."),
  INVALID_INITREFNUMBER(6, "initRefNumber length must be 32"),
  PAGE_LESS_THAN_ZERO(6, "page must not be less than 0"),
  PAGE_SIZE_LESS_THAN_ONE(6, "size must not be less than 1"),
  INVALID_CURRENCY(6, "Invalid currency"),

  MISSING_INITREFNUMBER(6, "initRefNumber is mandatory, initRefNumber length must be 32"),
  INVALID_TOKEN(7, "Token is invalid."),
  FAIL_TO_FETCH_ACCOUNT_DETAILS(8, "Getting user account information failed."),
  TRANSACTION_TO_UNAVAILABLE_ACCOUNT(9, "Transaction to unavailable account."),
  BALANCE_NOT_ENOUGH(10, "Not enough balance to do a transaction."),

  BALANCE_NOT_ENOUGH_INCLUDE_FEE(
      10, "Not enough balance to do a transaction because a fee will be applied."),
  TRANSACTION_EXCEED_AMOUNT_LIMIT(11, "The amount entered is below or exceed the allowed limit."),
  DUPLICATE_SUBMISSION_ID(12, "Duplicate submissionId."),
  INVALID_OTP(13, "Invalid OTP. Please try again."),
  OTP_EXPIRED(17, "OTP expired. Please try again."),
  KYC_NOT_VERIFIED(14, "Cannot link account due to your account not yet verified."),
  ACCOUNT_ALREADY_LINKED(15, "This account is already linked to another Bakong account."),
  AUTHENTICATION_LOCKED(16, "Too many incorrect authentication attempts. Please try again later."),

  OVER_DAILY_TRANSFER_LIMIT(21, "Transaction amount exceeds daily limit"),
  ACCOUNT_NOT_LINKED_WITH_BAKONG_ACCOUNT(
      32, "The Bakong account you are trying to transfer to is not linked to your account."),
  INIT_REFNUMBER_NOT_FOUND(
      12, "initRefNumber was not found or already used in another transaction."),

  ACC_NOT_LINKED(30, "Your credential is not associated with any account"),
  // CDRB error messages
  NO_SUCH_USER_FOUND(100, "User not found."),
  USER_ALREADY_EXIST(101, "User already exist."),
  FILE_PROCESSED(102, "The file input date has been processed"),
  BAD_REQUEST(103, "Bad Request"),

  // Generic error message
  CONSTRAINT_VIOLATION_ERROR(300, "Constraint violation exception."),
  DATA_NOT_FOUND(301, "Data not found."),
  DATA_STRUCTURE_INVALID(302, "Data structure invalid."),
  TOO_MANY_REQUEST_ERROR(429, "Too Many Requests");

  private final Integer code;
  private final String msg;
}
