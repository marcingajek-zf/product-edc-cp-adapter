package org.eclipse.tractusx.edc.cp.adapter.store.schema;

import org.eclipse.edc.sql.dialect.BaseSqlDialect;

import static java.lang.String.format;

public class BaseSqlDialectObjectStoreStatements implements ObjectStoreStatements {
  @Override
  public String getSaveObjectTemplate() {
    return format("INSERT INTO %s (%s, %s, %s, %s) VALUES(?, ?, ?, ?%s)",
        getObjectStoreTable(), getIdColumn(), getCreatedAtColumn(), getTypeColumn(), getObjectColumn(), getFormatJsonOperator());
  }

  @Override
  public String getFindByIdAndTypeTemplate() {
    return format("SELECT * FROM %s WHERE %s = ? AND %s = ?", getObjectStoreTable(), getIdColumn(), getTypeColumn());
  }

  @Override
  public String getFindByTypeTemplate() {
    return format("SELECT * FROM %s WHERE %s = ?", getObjectStoreTable(), getTypeColumn());
  }

  @Override
  public String getDeleteTemplate() {
    return format("DELETE FROM %s WHERE %s = ? AND %s = ?;", getObjectStoreTable(), getIdColumn(), getTypeColumn());
  }

  protected String getFormatJsonOperator() {
    return BaseSqlDialect.getJsonCastOperator();
  }
}
