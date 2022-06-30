package net.catenax.edc.tests;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.NonNull;

public class ConnectorFactory {
  private static final Map<String, Connector> CONNECTOR_CACHE = new HashMap<>();

  public static Connector byName(@NonNull final String name) {
    return CONNECTOR_CACHE.computeIfAbsent(
        name.toUpperCase(Locale.ROOT), (k) -> createConnector(name));
  }

  private static Connector createConnector(@NonNull final String name) {
    final Environment environment = Environment.byName(name);

    return new Connector(name, environment);
  }
}
