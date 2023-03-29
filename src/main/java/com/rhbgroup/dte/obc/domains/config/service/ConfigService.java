package com.rhbgroup.dte.obc.domains.config.service;

public interface ConfigService {

  <T> T getByConfigKey(String configKey, String valueKey, Class<T> clazz);

  ConfigService loadJSONValue(String configKey);

  <T> T getValue(String valueKey, Class<T> clazz);

  <T> T getByConfigKey(String configKey, Class<T> clazz);
}
