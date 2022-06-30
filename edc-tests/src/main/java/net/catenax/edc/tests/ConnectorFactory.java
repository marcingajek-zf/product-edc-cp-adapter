package net.catenax.edc.tests;

import java.util.Objects;

public class ConnectorFactory {
  public Connector fromName(String name) {
    if (Objects.equals("Plato", name)) {
      return new Connector("http://192.168.49.2:31275", "http://192.168.49.2:32000");
    }
    if (Objects.equals("Sokrates", name)) {
      return new Connector("http://192.168.49.2:31578", "http://192.168.49.2:31487");
    }
    throw new IllegalArgumentException("Unknown connector: " + name);
  }
}
