package features;

import io.cucumber.java.ParameterType;
import net.catenax.edc.tests.Connector;
import net.catenax.edc.tests.ConnectorFactory;

public class ParameterTypes {

    private final ConnectorFactory connectorFactory = new ConnectorFactory();

    @ParameterType("Plato|Sokrates")
    public Connector connector(String name) {
        return connectorFactory.fromName(name);
    }

}
