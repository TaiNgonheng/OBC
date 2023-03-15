package com.rhbgroup.dte.obc.domains.user.service;

import com.rhbgroup.dte.obc.model.ExchangeAccountResponseAllOfData;
import com.rhbgroup.dte.obc.model.UserModel;

public interface UserExchangeService {

  ExchangeAccountResponseAllOfData exchangeUser(UserModel userModel);

  String revokeToken(UserModel model);
}
