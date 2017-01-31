
package com.shinesolutions.healthcheck.servlets;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.hc.api.execution.HealthCheckExecutionOptions;
import org.apache.sling.hc.api.execution.HealthCheckExecutionResult;
import org.apache.sling.hc.api.execution.HealthCheckExecutor;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used to execute Sling Health Checks based on provided tags (if no tags
 * are provided, all registered health checks will be executed).
 *
 * Sample requests:
 * /system/health
 * /system/health?tags=devops
 * /system/health?tags=devops,security
 * /system/health?tags=devops,security&amp;combineTagsOr=false
 *
 * Sample response:
 * {
 *   "results": [
 *       {
 *           "name": "Smoke Health Check",
 *           "status": "OK",
 *           "timeMs": 0,
 *       }
 *   ]
 * }
 *
 *
 * Note: It is assumed that all /system/* paths are only accessible from a local
 * network and not routed to the Internet.
 *
 * More information:
 * https://sling.apache.org/documentation/bundles/sling-health-check-tool.html
 */
@SlingServlet(
    name = "Health Check Executor Servlet",
    generateComponent = true,
    methods = {"GET"},
    paths = {"/system/health"}
)
@Property(name = "sling.auth.requirements", value = "-/system/health")
public class HealthCheckExecutorServlet extends SlingSafeMethodsServlet {

    @Reference
    protected HealthCheckExecutor healthCheckExecutor;

    private static final String PARAM_TAGS = "tags";
    private static final String PARAM_COMBINE_TAGS_OR = "combineTagsOr";

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckExecutorServlet.class);

    @Activate
    protected void activate(ComponentContext context) {
        logger.debug("Starting HealthCheckExecutorServlet");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Stopping HealthCheckExecutorServlet");
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
                                                        throws ServletException, IOException {
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        // parse query parameters
        String tagsStr = StringUtils.defaultString(request.getParameter(PARAM_TAGS));
        String[] tags = tagsStr.split("[, ;]+");
        String combineTagsOr = StringUtils.defaultString(request.getParameter(PARAM_COMBINE_TAGS_OR), "true");

        // execute health checks
        HealthCheckExecutionOptions options = new HealthCheckExecutionOptions();
        options.setCombineTagsWithOr(Boolean.valueOf(combineTagsOr));
        List<HealthCheckExecutionResult> results = healthCheckExecutor.execute(options, tags);

        // check results
        boolean allOk = true;
        for(HealthCheckExecutionResult result : results) {
            if(!result.getHealthCheckResult().isOk()) {
                allOk = false;
                break;
            }
        }

        // set appropriate status code
        if(!allOk) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        // write out JSON response
        JSONObject resultJson = new JSONObject();
        try {
            generateResponse(results, resultJson);
        } catch(JSONException ex) {
            logger.error("Could not serialize result into JSON", ex);
        }
        response.getWriter().write(resultJson.toString());
    }

    /**
     * Creates a JSON representation of the given HealthCheckExecutionResult list.
     * @param executionResults
     * @param resultJson
     * @return
     * @throws JSONException
     */
    private static JSONObject generateResponse(List<HealthCheckExecutionResult> executionResults,
                                                JSONObject resultJson) throws JSONException {
        JSONArray resultsJsonArr = new JSONArray();
        resultJson.put("results", resultsJsonArr);

        for (HealthCheckExecutionResult healthCheckResult : executionResults) {
            JSONObject result = new JSONObject();
            result.put("name", healthCheckResult.getHealthCheckMetadata() != null ?
                               healthCheckResult.getHealthCheckMetadata().getName() : "");
            result.put("status", healthCheckResult.getHealthCheckResult().getStatus());
            result.put("timeMs", healthCheckResult.getElapsedTimeInMs());
            resultsJsonArr.put(result);
        }
        return resultJson;
    }
}
