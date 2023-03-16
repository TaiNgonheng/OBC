package com.rhbgroup.dte.obc.acount.controller;

import com.rhbgroup.dte.obc.acount.AbstractAccountTest;
import com.rhbgroup.dte.obc.domains.account.controller.AccountController;
import com.rhbgroup.dte.obc.domains.account.service.impl.AccountServiceImpl;
import com.rhbgroup.dte.obc.model.InitAccountResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest extends AbstractAccountTest {

  @InjectMocks AccountController accountController;

  @Mock AccountServiceImpl accountService;

  @Test
  void testIntiAccount_Success2xx() {
    Mockito.when(accountService.initLinkAccount(Mockito.any()))
        .thenReturn(mockInitAccountResponse());
    ResponseEntity<InitAccountResponse> responseEntity =
        accountController.initLinkAccount(mockInitAccountRequest());

    Assertions.assertNotNull(responseEntity);
    Assertions.assertNotNull(responseEntity.getBody());

    Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    Assertions.assertEquals(0, responseEntity.getBody().getStatus().getCode());
  }

  @Test
  void testIntiAccount_InternalServerError500_Failed() {
    // TODO
  }

  @Test
  void testIntiAccount_BadRequest400_Failed() {
    // TODO
  }

  @Test
  void testIntiAccount_Unauthorized401_Failed() {
    // TODO
  }
}
