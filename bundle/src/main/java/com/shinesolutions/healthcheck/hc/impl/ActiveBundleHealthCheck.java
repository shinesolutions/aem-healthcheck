
package com.shinesolutions.healthcheck.hc.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.sling.hc.annotations.SlingHealthCheck;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.util.FormattingResultLog;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Health Check to test if all Bundles are active.
 */
@SlingHealthCheck(
        name = "Bundle Health Check",
        mbeanName = "bundleHC",
        description = "This health check scans the current OSGi bundles and reports if there is any inactive bundles.",
        tags = {"deep"}
)
public class ActiveBundleHealthCheck implements HealthCheck {
    private BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext context) {
        bundleContext = context.getBundleContext();
    }

    @Deactivate
    protected void deactivate() {
        bundleContext = null;
    }

    @Override
    public Result execute() {
        FormattingResultLog resultLog = new FormattingResultLog();
        int inactiveBundles = 0;
        int activeBundles = 0;
        Bundle[] bundles = bundleContext.getBundles();

        // TODO: Do we want the bundle HC to be unhealthy if any bundle is not active?
        for (Bundle bundle : bundles) {
            if (!isActiveBundle(bundle)) {
                ++inactiveBundles;
                resultLog.warn("Bundle {} is not active. It is in state {}.", bundle.getSymbolicName(), bundle.getState());
            } else {
                ++activeBundles;
            }
        }

        resultLog.info("There are a total of {} active Bundles.", activeBundles);

        if (inactiveBundles == 0) resultLog.info("There are no inactive Bundles");

        return new Result(resultLog);
    }

    /**
     * Checks if bundle is currently active, fragmented, or if it's activation is lazy; all of which count towards the active state
     */
    private static boolean isActiveBundle(Bundle bundle) {
        return (bundle.getState() == Bundle.ACTIVE ||
        bundle.getHeaders().get("Fragment-Host") != null) ||
                (bundle.getHeaders().get("Bundle-ActivationPolicy") != null &&
                bundle.getHeaders().get("Bundle-ActivationPolicy").equals("lazy"));
    }
}
