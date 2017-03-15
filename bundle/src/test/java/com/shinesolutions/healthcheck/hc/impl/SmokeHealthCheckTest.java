
package com.shinesolutions.healthcheck.hc.impl;

import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SmokeHealthCheckTest {
    
    @Test
    public void testExecute() {
        HealthCheck healthCheck = new SmokeHealthCheck();
        Result result = healthCheck.execute();
        
        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());
        assertTrue("Message should say instance is ready", result.iterator().next().getMessage().contains("Instance is ready at"));
    }
}
