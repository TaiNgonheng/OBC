package com.rhbgroup.dte.obc.domains.config.service.impl;

import com.rhbgroup.dte.obc.common.ResponseMessage;
import com.rhbgroup.dte.obc.domains.config.repository.ConfigRepository;
import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import com.rhbgroup.dte.obc.domains.config.service.ConfigService;
import com.rhbgroup.dte.obc.exceptions.BizException;
import java.util.List;
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

    try {
      JSONObject configValue = new JSONObject(findByConfigKey(configKey).getConfigValue());
      return clazz.cast(configValue.get(valueKey));

    } catch (Exception e) {
      throw new BizException(ResponseMessage.DATA_STRUCTURE_INVALID);
    }
  }

  @Override
  public String getByConfigKey(String configKey, String valueKey) {

    try {
      String configJson = findByConfigKey(configKey).getConfigValue();
      JSONObject configValue = new JSONObject(configJson);
      return configValue.getString(valueKey);

    } catch (Exception e) {
      throw new BizException(ResponseMessage.DATA_STRUCTURE_INVALID);
    }
  }

  @Override
  public ConfigService loadJSONValue(String configKey) {

    try {
      this.jsonValue = new JSONObject(findByConfigKey(configKey).getConfigValue());
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

  @Override
  public String getStringValue(String valueKey) {

    try {
      return jsonValue.getString(valueKey);

    } catch (JSONException e) {
      throw new BizException(ResponseMessage.DATA_STRUCTURE_INVALID);
    }
  }

  @Override
  public List<ConfigEntity> findByServicePrefix(String prefix) {
    return configRepository.findByConfigKeyIgnoreCaseStartingWith(prefix);
  }

  @Override
  public ConfigEntity filterByServiceKey(List<ConfigEntity> configEntities, String key) {
    return configEntities.stream()
        .filter(configEntity -> configEntity.getConfigKey().equals(key))
        .findFirst()
        .orElse(new ConfigEntity());
  }

  private ConfigEntity findByConfigKey(String configKey) {
    return configRepository
        .getByConfigKey(configKey)
        .orElseThrow(() -> new BizException(ResponseMessage.DATA_NOT_FOUND));
  }

  public JSONObject getJsonValue() {
    return jsonValue;
  }

  public void setJsonValue(JSONObject jsonValue) {
    this.jsonValue = jsonValue;
  }
}
