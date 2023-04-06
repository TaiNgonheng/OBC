package com.rhbgroup.dte.obc.transaction.controller;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dte.obc.api.TransactionApiController;
import com.rhbgroup.dte.obc.api.TransactionApiDelegate;
import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.constants.AppConstants;
import com.rhbgroup.dte.obc.exceptions.GlobalExceptionHandler;
import com.rhbgroup.dte.obc.exceptions.UserAuthenticationException;
import com.rhbgroup.dte.obc.model.InitTransactionRequest;
import com.rhbgroup.dte.obc.model.InitTransactionResponse;
import com.rhbgroup.dte.obc.transaction.AbstractTransactionTest;
import java.nio.charset.StandardCharsets;
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
  void testInitTransaction_Success_401_Unauthorized() throws Exception {

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
  void testInitTransaction_Success_400_MissingMandatoryFields() throws Exception {

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
}
