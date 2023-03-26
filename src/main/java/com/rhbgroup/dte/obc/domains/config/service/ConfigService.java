package com.rhbgroup.dte.obc.domains.config.service;

public interface ConfigService {

  <T> T getByConfigKey(String configKey, String valueKey, Class<T> clazz);

  String getByConfigKey(String configKey, String valueKey);

  ConfigService loadJSONValue(String configKey);

  <T> T getValue(String valueKey, Class<T> clazz);

  String getStringValue(String valueKey);
}
