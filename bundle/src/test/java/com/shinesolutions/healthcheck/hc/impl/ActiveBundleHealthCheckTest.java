package com.shinesolutions.healthcheck.hc.impl;

import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ActiveBundleHealthCheckTest {
    @Mock
    private BundleContext bundleContext;

    @Mock
    private ComponentContext componentContext;

    @InjectMocks
    private HealthCheck healthCheck = new ActiveBundleHealthCheck();

    private Bundle bundle1 = mock(Bundle.class);
    private Bundle bundle2 = mock(Bundle.class);

    private Dictionary<String,String> headers1 = mock(Dictionary.class);
    private Dictionary<String,String> headers2 = mock(Dictionary.class);


    private Bundle[] bundles = new Bundle[] { bundle1, bundle2 };
    private String SYMBOLIC_NAME = "com.day.cq.dam.dam-webdav-support";
    private String[] ignoredBundles = new String[]{SYMBOLIC_NAME};


    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(bundle1.getHeaders()).thenReturn(headers1);
        when(bundle2.getHeaders()).thenReturn(headers2);

        when(bundle1.getState()).thenReturn(Bundle.ACTIVE);
        when(bundle1.getBundleContext()).thenReturn(bundleContext);
        when(bundle1.getSymbolicName()).thenReturn("Bundle 1");

        when(bundle2.getState()).thenReturn(Bundle.ACTIVE);
        when(bundle2.getBundleContext()).thenReturn(bundleContext);
        when(bundle2.getSymbolicName()).thenReturn("Bundle 2");

        when(bundleContext.getBundles()).thenReturn(bundles);
    }

    @Test
    public void testInactiveBundle() {
        when(bundle1.getState()).thenReturn(Bundle.INSTALLED);
        Result result = healthCheck.execute();
        assertEquals("Status should be WARN", Result.Status.WARN, result.getStatus());
        String msg = "Bundle Bundle 1 is not active. It is in state 2.";
        assertTrue("Message should say bundle is not active", result.iterator().next().getMessage().contains(msg));
    }

    @Test
    public void testFragmentBundle() {
        when(bundle1.getState()).thenReturn(Bundle.START_TRANSIENT);
        when(bundle1.getHeaders().get("Fragment-Host")).thenReturn("True");
        Result result = healthCheck.execute();
        assertEquals("Status should be Ok", Result.Status.OK, result.getStatus());
        String msg = "All bundles are considered active";
        assertTrue("Message should say there are no inactive bundles", result.toString().contains(msg));
    }

    @Test
    public void testLazyActivationBundle() {
        when(bundle1.getState()).thenReturn(Bundle.INSTALLED);
        when(bundle1.getHeaders().get("Bundle-ActivationPolicy")).thenReturn("lazy");
        Result result = healthCheck.execute();
        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
    }

    @Test
    public void testIgnoredBundles() {
        ActiveBundleHealthCheck abhc = spy(activeBundleHealthCheck());

        when(componentContext.getProperties()).thenReturn(new Hashtable(){{
            put(ActiveBundleHealthCheck.IGNORED_BUNDLES, ignoredBundles);
        }});

        when(componentContext.getBundleContext()).thenReturn(bundleContext);

        when(bundle1.getState()).thenReturn(Bundle.RESOLVED);
        when(bundle1.getSymbolicName()).thenReturn(SYMBOLIC_NAME);

        abhc.activate(componentContext);

        Result result = abhc.execute();
        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        String msg = "The following bundles will be ignored: [" + SYMBOLIC_NAME + "]";
        assertTrue("Message should contain the ignored bundles", result.toString().contains(msg));

    }

    @Test
    public void testEmptyIgnoredBundlesConfig() {
        ActiveBundleHealthCheck abhc = spy(activeBundleHealthCheck());
        when(componentContext.getProperties()).thenReturn(new Hashtable<>());

        when(componentContext.getBundleContext()).thenReturn(bundleContext);

        abhc.activate(componentContext);

        Result result = abhc.execute();
        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        String msg = "The following bundles will be ignored: [" + SYMBOLIC_NAME + "]";
        assertFalse("Message should contain the ignored bundles", result.toString().contains(msg));
    }

    @Test
    public void testExecute() {
        Result result = healthCheck.execute();

        String resultLog = "ResultLog: [INFO All bundles are considered active]";
        assertEquals("Result [status=" + Result.Status.OK + ", resultLog=" + resultLog + "]", result.toString());

        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());

    }

    private ActiveBundleHealthCheck activeBundleHealthCheck() {
        return new ActiveBundleHealthCheck();
    }
}
