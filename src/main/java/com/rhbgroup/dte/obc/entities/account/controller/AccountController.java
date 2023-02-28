package com.rhbgroup.dte.obc.entities.account.controller;

import com.rhbgroup.dte.obc.common.ResponseWrapper;
import com.rhbgroup.dte.obc.common.utils.func.Functions;
import com.rhbgroup.dte.obc.entities.account.controller.request.AccountRequest;
import com.rhbgroup.dte.obc.entities.account.controller.response.AccountResponse;
import com.rhbgroup.dte.obc.entities.account.interactor.AccountInteractor;
import com.rhbgroup.dte.obc.entities.account.mapper.AccountMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authorization")
public class AccountController {
    private final AccountInteractor accountInteractor;
    private final AccountMapper accountMapper;

    @PostMapping("/init-link-account")
    public ResponseEntity<ResponseWrapper<AccountResponse>> initLink(@RequestBody AccountRequest request) {

        return Functions.of(accountInteractor::authenticate)
                .andThen(authentication -> accountMapper.toAccountResponse(request, authentication))
                .andThen(ResponseWrapper::ok)
                .andThen(ResponseEntity::ok)
                .apply(request);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseWrapper<String>> getOtp() {
        return ResponseEntity.ok(ResponseWrapper.ok("012345"));
    }
}
