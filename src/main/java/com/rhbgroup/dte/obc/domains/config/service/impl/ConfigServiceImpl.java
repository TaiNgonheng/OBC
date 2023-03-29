package com.rhbgroup.dte.obc.domains.config.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

  private final ConfigRepository configRepository;

  private JSONObject jsonValue;

  @Override
  public <T> T getByConfigKey(String configKey, String valueKey, Class<T> clazz) {

    ConfigEntity requireOtpConfig =
        configRepository
            .getByConfigKey(configKey)
            .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
    try {
      JSONObject configValue = new JSONObject(requireOtpConfig.getConfigValue());
      return clazz.cast(configValue.get(valueKey));

    } catch (Exception e) {
      throw new BizException(ResponseMessage.DATA_STRUCTURE_INVALID);
    }
  }

  @Override
  public ConfigService loadJSONValue(String configKey) {

    ConfigEntity requireOtpConfig =
        configRepository
            .getByConfigKey(configKey)
            .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
    try {
      this.jsonValue = new JSONObject(requireOtpConfig.getConfigValue());
      return this;

    } catch (Exception e) {
      throw new BizException(ResponseMessage.DATA_STRUCTURE_INVALID);
    }
  }

  @Override
  public <T> T getValue(String valueKey, Class<T> clazz) {

    try {
      return clazz.cast(jsonValue.get(valueKey));

    } catch (JSONException e) {
      throw new BizException(ResponseMessage.DATA_STRUCTURE_INVALID);
    }
  }

  public void setJsonValue(JSONObject jsonValue) {
    this.jsonValue = jsonValue;
  }
}
