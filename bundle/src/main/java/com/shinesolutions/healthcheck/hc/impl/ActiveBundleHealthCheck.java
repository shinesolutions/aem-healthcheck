
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

    private static final String BUNDLE_FRAGMENT_HOST     = "Fragment-Host";
    private static final String BUNDLE_ACTIVATION_POLICY = "Bundle-ActivationPolicy";
    private static final String LAZY_ACTIVATION_POLICY   = "lazy";

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
        Bundle[] bundles = bundleContext.getBundles();

        for (Bundle bundle : bundles) {
            if (!isActiveBundle(bundle)) {
                inactiveBundles++;
                resultLog.warn("Bundle {} is not active. It is in state {}.", bundle.getSymbolicName(), bundle.getState());
            }
        }

        if (inactiveBundles > 0) {
            resultLog.warn("There are {} inactive Bundles", inactiveBundles);
        } else {
            resultLog.info("All bundles are considered active");
        }

        return new Result(resultLog);
    }

    /**
     * Checks whether the provided bundle is active. A bundle is considered active if it meets the following criteria:
     * - the bundle is active, or
     * - it is a fragment bundle, or
     * - it has a lazy activation policy
     *
     * @param bundle
     * @return
     */
    private static boolean isActiveBundle(Bundle bundle) {
        return (bundle.getState() == Bundle.ACTIVE ||
        bundle.getHeaders().get(BUNDLE_FRAGMENT_HOST) != null) ||
                (bundle.getHeaders().get(BUNDLE_ACTIVATION_POLICY) != null &&
                bundle.getHeaders().get(BUNDLE_ACTIVATION_POLICY).equals(LAZY_ACTIVATION_POLICY));
    }
}
