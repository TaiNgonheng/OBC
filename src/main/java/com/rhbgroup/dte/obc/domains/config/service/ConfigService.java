package com.rhbgroup.dte.obc.domains.config.service;

import com.rhbgroup.dte.obc.domains.config.repository.entity.ConfigEntity;
import java.util.List;

public interface ConfigService {

  <T> T getByConfigKey(String configKey, String valueKey, Class<T> clazz);

  String getByConfigKey(String configKey, String valueKey);

  ConfigService loadJSONValue(String configKey);

  <T> T getValue(String valueKey, Class<T> clazz);

  String getStringValue(String valueKey);

  List<ConfigEntity> findByServicePrefix(String servicePrefix);

  ConfigEntity filterByServiceKey(List<ConfigEntity> configEntities, String key);

  <T> T getByConfigKey(String configKey, Class<T> clazz);
}
