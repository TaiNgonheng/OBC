package com.rhbgroup.dte.obc.domains.account.mapper;

import com.rhbgroup.dte.obc.model.AccountModel;
import com.rhbgroup.dte.obc.model.InitAccountRequest;
import com.rhbgroup.dte.obc.model.UserModel;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AccountMapper {

  default AccountModel toModel(InitAccountRequest request) {
    return new AccountModel()
        .mobileNo(request.getPhoneNumber())
        .user(
            new UserModel()
                .loginType(request.getLoginType())
                .username(request.getLogin())
                .password(request.getKey()))
        .bakongId(request.getBakongAccId());
  }
}
