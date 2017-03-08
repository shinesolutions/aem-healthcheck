
package com.shinesolutions.healthcheck.hc.impl;

import java.util.Date;
import org.apache.sling.hc.annotations.SlingHealthCheck;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.util.FormattingResultLog;

/**
 * Sample Health Check to test whether an AEM instance is ready to serve content.
 */
@SlingHealthCheck(
    name = "Smoke Health Check",
    mbeanName = "smokeHC",
    description = "This health check determines if an instance is ready to serve requests",
    tags = {"shallow", "devops", "deep"} // TODO: The deep tag is just a placeholder for when the actual healthchecks are done
)
public class SmokeHealthCheck implements HealthCheck {

    @Override
    public Result execute() {
        FormattingResultLog resultLog = new FormattingResultLog();
        resultLog.info("Instance is ready at {}", new Date());
        return new Result(resultLog);
    }
}
