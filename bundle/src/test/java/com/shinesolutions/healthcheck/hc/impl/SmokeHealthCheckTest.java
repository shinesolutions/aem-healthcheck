
package com.shinesolutions.healthcheck.hc.impl;

import junit.framework.Assert;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.junit.Test;

public class SmokeHealthCheckTest {
    
    @Test
    public void testExecute() {
        HealthCheck healthCheck = new SmokeHealthCheck();
        Result result = healthCheck.execute();
        
        Assert.assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        Assert.assertEquals("Result should be ok", true, result.isOk());
        Assert.assertTrue("Message should say instance is ready", result.iterator().next().getMessage().contains("Instance is ready at"));
    }
}
