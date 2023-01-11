package org.eclipse.tractusx.edc.cp.adapter.store.schema;

public interface ObjectStoreStatements {
  default String getObjectStoreTable() {
    return "edc_cpadapter_object_store";
  }

  default String getIdColumn() {
    return "id";
  }

  default String getCreatedAtColumn() {
    return "created_at";
  }

  default String getTypeColumn() {
    return "type";
  }

  default String getObjectColumn() {
    return "object";
  }

  String getSaveObjectTemplate();

  String getFindByIdAndTypeTemplate();

  String getFindByTypeTemplate();

  String getDeleteTemplate();
}
