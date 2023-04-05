package com.rhbgroup.dte.obc.domains.transaction.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.common.func.Functions;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapper;
import com.rhbgroup.dte.obc.domains.transaction.mapper.TransactionMapperImpl;
import com.rhbgroup.dte.obc.domains.transaction.repository.TransactionRepository;
import com.rhbgroup.dte.obc.domains.transaction.service.TransactionService;
import com.rhbgroup.dte.obc.domains.user.service.UserProfileService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import com.rhbgroup.dte.obc.model.FinishTransactionRequest;
import com.rhbgroup.dte.obc.model.FinishTransactionResponse;
import com.rhbgroup.dte.obc.model.TransactionModel;
import com.rhbgroup.dte.obc.model.UserModel;
import com.rhbgroup.dte.obc.security.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final JwtTokenUtils jwtTokenUtils;
  private final UserProfileService userProfileService;
  private final TransactionRepository transactionRepository;
  private final TransactionMapper transactionMapper = new TransactionMapperImpl();

  @Override
  public void save(TransactionModel transactionModel) {
    Functions.of(transactionMapper::toEntity)
        .andThen(transactionRepository::save)
        .apply(transactionModel);
  }

  @Override
  public FinishTransactionResponse finishTransaction(
      String authorization, FinishTransactionRequest finishTransactionRequest) {
    Long userId = Long.parseLong(jwtTokenUtils.getUserId(authorization));
    UserModel userProfile = userProfileService.findByUserId(userId);
    if (finishTransactionRequest.getKey().equals(userProfile.getPassword())) {
      throw new BizException(ResponseMessage.AUTHENTICATION_FAILED);
    }
    return null;
  }
}
