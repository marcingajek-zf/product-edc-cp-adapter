package net.catenax.edc.tests;

import java.util.Objects;

public class ConnectorFactory {
    public Connector fromName(String name) {
        if (Objects.equals("Plato", name)) {
            return new Connector(("https://localhost:8181"));
        }
        if (Objects.equals("Sokrates", name)) {
            return new Connector(("https://localhost:9191"));
        }
        throw new IllegalArgumentException("Unknown connector: " + name);
    }
}
