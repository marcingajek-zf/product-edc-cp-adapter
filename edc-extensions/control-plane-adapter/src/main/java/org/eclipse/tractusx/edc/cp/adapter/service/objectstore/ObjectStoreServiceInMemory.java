package org.eclipse.tractusx.edc.cp.adapter.service.objectstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ObjectStoreServiceInMemory implements ObjectStoreService {
  private final ObjectMapper mapper;
  private final Map<String, String> map = new HashMap<>();

  @Override
  public void put(String key, ObjectType objectType, Object object) {
    try {
      map.put(getKey(key, objectType), mapper.writeValueAsString(object));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new IllegalArgumentException();
    }
  }

  @Override
  public <T> T get(String key, ObjectType objectType, Class<T> type) {
    String json = map.get(getKey(key, objectType));
    if (json == null) {
      return null;
    }

    T object = null;
    try {
      object = mapper.readValue(json, type);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return object;
  }

  @Override
  public void remove(String key, ObjectType objectType) {
    map.remove(getKey(key, objectType));
  }

  private String getKey(String key, ObjectType objectType) {
    return objectType.name() + key;
  }
}
