
package com.shinesolutions.healthcheck.hc.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.hc.annotations.SlingHealthCheck;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.util.FormattingResultLog;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import java.util.Arrays;

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

    @Property(label = "Ignored Bundles", description = "The bundles that will be ignored in the Active Bundle Health-Check")
    protected static final String IGNORED_BUNDLES = "bundles.ignored";
    protected static String[] ignoredBundles;

    @Activate
    protected void activate(ComponentContext context) {
        bundleContext = context.getBundleContext();
        ignoredBundles = PropertiesUtil.toStringArray(context.getProperties().get(IGNORED_BUNDLES));
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
            if ((!isActiveBundle(bundle)) && !isIgnoredBundle(bundle)) {
                inactiveBundles++;
                resultLog.warn("Bundle {} is not active. It is in state {}.", bundle.getSymbolicName(), bundle.getState());
            }
        }

        if (ignoredBundles != null) {
            resultLog.debug("The following bundles will be ignored: {}", Arrays.toString(ignoredBundles));
        }

        if (inactiveBundles > 0) {
            resultLog.warn("There are {} inactive Bundles", inactiveBundles);
        } else {
            resultLog.info("All bundles are considered active");
        }

        return new Result(resultLog);
    }

    /**
     * Checks whether the provided bundle is in a string Array of ignored bundles.
     *
     * @param bundle
     * @return
     */
    private static boolean isIgnoredBundle(Bundle bundle) {
        return (ignoredBundles != null &&
                Arrays.asList(ignoredBundles).contains(bundle.getSymbolicName()));
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
