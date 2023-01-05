package org.eclipse.tractusx.edc.cp.adapter.store.schema.postgres;

import org.eclipse.edc.sql.dialect.PostgresDialect;
import org.eclipse.tractusx.edc.cp.adapter.store.schema.BaseSqlDialectObjectStoreStatements;

public class PostgresDialectObjectStoreStatements extends BaseSqlDialectObjectStoreStatements {
  /**
   * Overridable operator to convert strings to JSON. For postgres, this is the "::json" operator
   */
  @Override
  protected String getFormatJsonOperator() {
    return PostgresDialect.getJsonCastOperator();
  }
}
