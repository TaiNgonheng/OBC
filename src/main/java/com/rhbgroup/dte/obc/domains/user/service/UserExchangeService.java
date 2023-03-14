package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.model.UserModel;

public interface UserExchangeService {

  String exchangeUser(UserModel userModel);

  String revokeToken(UserModel model);
}
