package net.catenax.edc.cp.adapter;

import org.eclipse.dataspaceconnector.spi.EdcSetting;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

public class ApiAdapterConfig {
    @EdcSetting(required = true)
    private static final String CONTROL_PLANE_URL = "create.config.for.control.plane.url";

    @EdcSetting(required = true)
    private static final String CONTROL_PLANE_PASSWORD = "create.config.for.control.plane.pass";

    private final ServiceExtensionContext context;

    public ApiAdapterConfig(ServiceExtensionContext context) {
        this.context = context;
    }

    public String getControlPlaneUrl() {
        return context.getSetting(CONTROL_PLANE_URL, null);
    }

    public String getControlPlanePassword() {
        return context.getSetting(CONTROL_PLANE_PASSWORD, null);
    }
}
