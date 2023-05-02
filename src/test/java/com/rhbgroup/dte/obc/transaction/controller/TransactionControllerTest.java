package com.rhbgroup.dte.obc.transaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.api.TransactionApiController;
import com.rhbgroup.dte.obc.api.TransactionApiDelegate;
import com.rhbgroup.dte.obc.common.ResponseHandler;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.exceptions.GlobalExceptionHandler;
import com.rhbgroup.dte.obc.exceptions.InternalException;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsRequest;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponse;
import com.rhbgroup.dte.obc.model.GetAccountTransactionsResponseAllOfData;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.transaction.AbstractTransactionTest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest extends AbstractTransactionTest {

  MockMvc mockMvc;

  @Mock TransactionApiDelegate transactionApiDelegate;

  @InjectMocks TransactionApiController transactionApiController;

  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(transactionApiController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    reset(transactionApiDelegate);
  }

  @Test
  void testInitTransaction_Success_200() throws Exception {

    InitTransactionRequest mockRequest = mockInitTransactionRequest();
    when(transactionApiDelegate.initTransaction(mockRequest))
        .thenReturn(ResponseEntity.ok(mockInitTransactionResponse()));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/init-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    InitTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, InitTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNotNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, initTransactionResponse.getStatus().getCode());
    Assertions.assertNotNull(initTransactionResponse.getData().getInitRefNumber());
    Assertions.assertEquals(
        mockRequest.getAmount(), initTransactionResponse.getData().getDebitAmount());
  }

  @Test
  void testInitTransaction_Failed_401_Unauthorized() throws Exception {

    InitTransactionRequest mockRequest = mockInitTransactionRequest();
    when(transactionApiDelegate.initTransaction(mockRequest))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/init-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockRequest)))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    InitTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, InitTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.ERROR, initTransactionResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getCode().toString(),
        initTransactionResponse.getStatus().getErrorCode());
    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getMsg(),
        initTransactionResponse.getStatus().getErrorMessage());
  }

  @Test
  void testInitTransaction_Failed_400_MissingMandatoryFields() throws Exception {

    InitTransactionRequest invalidRequest = new InitTransactionRequest();

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/init-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(invalidRequest)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    InitTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, InitTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.ERROR, initTransactionResponse.getStatus().getCode());
    Assertions.assertEquals(
        ResponseMessage.MANDATORY_FIELD_MISSING.getCode().toString(),
        initTransactionResponse.getStatus().getErrorCode());
    Assertions.assertEquals(
        ResponseMessage.MANDATORY_FIELD_MISSING.getMsg(),
        initTransactionResponse.getStatus().getErrorMessage());
  }

  @Test
  void testFinishTransaction_Success_200() throws Exception {

    FinishTransactionRequest mockRequest = mockFinishTransactionRequest();
    when(transactionApiDelegate.finishTransaction(mockRequest))
        .thenReturn(ResponseEntity.ok(mockFinishTransactionResponse()));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/finish-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    FinishTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, FinishTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNotNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, initTransactionResponse.getStatus().getCode());

    Assertions.assertNotNull(initTransactionResponse.getData().getTransactionHash());
  }

  @Test
  void testFinishTransaction_Success_200_ButCoreTransactionFailed() throws Exception {

    FinishTransactionRequest mockRequest = mockFinishTransactionRequest();

    // Mock core failed result
    FinishTransactionResponse mockResponse = mockFinishTransactionResponse();
    mockResponse.getData().setTransactionDate(null);
    mockResponse.getData().setTransactionHash(null);

    when(transactionApiDelegate.finishTransaction(mockRequest))
        .thenReturn(ResponseEntity.ok(mockResponse));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/finish-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    FinishTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, FinishTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNotNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, initTransactionResponse.getStatus().getCode());

    Assertions.assertNotNull(initTransactionResponse.getData().getTransactionId());
    Assertions.assertNull(initTransactionResponse.getData().getTransactionHash());
    Assertions.assertNull(initTransactionResponse.getData().getTransactionDate());
  }

  @Test
  void testFinishTransaction_Failed_401_Unauthorized() throws Exception {

    when(transactionApiDelegate.finishTransaction(any()))
        .thenThrow(new UserAuthenticationException(ResponseMessage.AUTHENTICATION_FAILED));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/finish-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockFinishTransactionRequest())))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    FinishTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, FinishTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.ERROR, initTransactionResponse.getStatus().getCode());

    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getCode().toString(),
        initTransactionResponse.getStatus().getErrorCode());

    Assertions.assertEquals(
        ResponseMessage.AUTHENTICATION_FAILED.getMsg(),
        initTransactionResponse.getStatus().getErrorMessage());
  }

  @Test
  void testFinishTransaction_Failed_500_InternalServerError() throws Exception {

    when(transactionApiDelegate.finishTransaction(any()))
        .thenThrow(new InternalException(ResponseMessage.INTERNAL_SERVER_ERROR));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/finish-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockFinishTransactionRequest())))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    FinishTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, FinishTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.ERROR, initTransactionResponse.getStatus().getCode());

    Assertions.assertEquals(
        ResponseMessage.INTERNAL_SERVER_ERROR.getCode().toString(),
        initTransactionResponse.getStatus().getErrorCode());

    Assertions.assertEquals(
        ResponseMessage.INTERNAL_SERVER_ERROR.getMsg(),
        initTransactionResponse.getStatus().getErrorMessage());
  }

  @Test
  void testFinishTransaction_Failed_400_OtpInvalid() throws Exception {

    when(transactionApiDelegate.finishTransaction(any()))
        .thenThrow(new BizException(ResponseMessage.INVALID_TOKEN));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/finish-transaction")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockFinishTransactionRequest())))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    FinishTransactionResponse initTransactionResponse =
        objectMapper.readValue(contentAsString, FinishTransactionResponse.class);

    Assertions.assertNotNull(initTransactionResponse.getStatus());
    Assertions.assertNull(initTransactionResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.ERROR, initTransactionResponse.getStatus().getCode());

    Assertions.assertEquals(
        ResponseMessage.INVALID_TOKEN.getCode().toString(),
        initTransactionResponse.getStatus().getErrorCode());

    Assertions.assertEquals(
        ResponseMessage.INVALID_TOKEN.getMsg(),
        initTransactionResponse.getStatus().getErrorMessage());
  }

  @Test
  void testQueryTransactionHistory_Success_200() throws Exception {

    GetAccountTransactionsRequest mockRequest = mockAccountTransactionRequest();

    when(transactionApiDelegate.queryTransactionHistory(mockRequest))
        .thenReturn(ResponseEntity.ok(mockAccountTransactionResponse()));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/account-transactions")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    GetAccountTransactionsResponse accountTransactionsResponse =
        objectMapper.readValue(contentAsString, GetAccountTransactionsResponse.class);

    Assertions.assertNotNull(accountTransactionsResponse.getStatus());
    Assertions.assertNotNull(accountTransactionsResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, accountTransactionsResponse.getStatus().getCode());

    Assertions.assertTrue(
        accountTransactionsResponse.getData().getTotalElement().intValue()
            <= mockRequest.getSize());

    Assertions.assertEquals(
        accountTransactionsResponse.getData().getTotalElement().intValue(),
        accountTransactionsResponse.getData().getTransactions().size());
  }

  @Test
  void testQueryTransactionHistory_Success_200_NoTransactionFound() throws Exception {

    GetAccountTransactionsRequest mockRequest = mockAccountTransactionRequest();

    when(transactionApiDelegate.queryTransactionHistory(mockRequest))
        .thenReturn(
            ResponseEntity.ok(
                new GetAccountTransactionsResponse()
                    .status(ResponseHandler.ok())
                    .data(
                        new GetAccountTransactionsResponseAllOfData()
                            .totalElement(0L)
                            .transactions(Collections.emptyList()))));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/account-transactions")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockRequest)))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    GetAccountTransactionsResponse accountTransactionsResponse =
        objectMapper.readValue(contentAsString, GetAccountTransactionsResponse.class);

    Assertions.assertNotNull(accountTransactionsResponse.getStatus());
    Assertions.assertNotNull(accountTransactionsResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.SUCCESS, accountTransactionsResponse.getStatus().getCode());

    Assertions.assertEquals(0, accountTransactionsResponse.getData().getTotalElement().intValue());

    Assertions.assertEquals(
        accountTransactionsResponse.getData().getTotalElement().intValue(),
        accountTransactionsResponse.getData().getTransactions().size());
  }

  @Test
  void testQueryTransactionHistory_Failed_500_InternalServerError() throws Exception {

    when(transactionApiDelegate.queryTransactionHistory(any()))
        .thenThrow(new InternalException(ResponseMessage.INTERNAL_SERVER_ERROR));

    MockHttpServletResponse response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/account-transactions")
                    .header(HttpHeaders.AUTHORIZATION, mockBearerString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(objectMapper.writeValueAsBytes(mockAccountTransactionRequest())))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists())
            .andReturn()
            .getResponse();

    String contentAsString = response.getContentAsString();
    GetAccountTransactionsResponse accountTransactionsResponse =
        objectMapper.readValue(contentAsString, GetAccountTransactionsResponse.class);

    Assertions.assertNotNull(accountTransactionsResponse.getStatus());
    Assertions.assertNull(accountTransactionsResponse.getData());

    Assertions.assertEquals(
        AppConstants.Status.ERROR, accountTransactionsResponse.getStatus().getCode());

    Assertions.assertEquals(
        ResponseMessage.INTERNAL_SERVER_ERROR.getCode().toString(),
        accountTransactionsResponse.getStatus().getErrorCode());

    Assertions.assertEquals(
        ResponseMessage.INTERNAL_SERVER_ERROR.getMsg(),
        accountTransactionsResponse.getStatus().getErrorMessage());
  }
}
