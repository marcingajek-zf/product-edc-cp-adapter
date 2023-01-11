package org.eclipse.tractusx.edc.cp.adapter.service.objectstore;

import java.util.List;

public interface ObjectStoreService {
  void put(String key, ObjectType objectType, Object object);
  <T> T get(String key, ObjectType objectType, Class<T> type);
  void remove(String key, ObjectType objectType);
  <T> List<T> get(ObjectType objectType, Class<T> type);
}
