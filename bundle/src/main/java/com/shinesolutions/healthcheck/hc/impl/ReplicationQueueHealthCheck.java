package com.shinesolutions.healthcheck.hc.impl;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationQueue;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.hc.annotations.SlingHealthCheck;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.util.FormattingResultLog;

import java.util.Map;

/**
 * Health Check to test the replication queue status and validity.
 */
@SlingHealthCheck(
        name="Replication Queue Health Check",
        mbeanName="replicationQueueHC",
        description="This health check checks the replication queue for any disabled or invalid agent.",
        tags={"deep"}
)
public class ReplicationQueueHealthCheck implements HealthCheck {
    @Reference
    private AgentManager agentManager;

    public Result execute() {
        FormattingResultLog resultLog = new FormattingResultLog();
        for (Map.Entry<String, Agent> entry : agentManager.getAgents().entrySet()) {
            Agent agent = entry.getValue();

            if(agent.isValid()) {

                // TODO: Do we want to warn if a replication agent is not currently enabled?
                if (agent.isEnabled()) {
                    ReplicationQueue replicationQueue = agent.getQueue();

                    // TODO: Do we want to consider the block an unhealthy status? or set up a blocked time frame for considering it healthy?
                    // Returns the time when the next retry is performed if the queue is blocked or 0 otherwise.
                    if(replicationQueue.getStatus().getNextRetryTime() != 0) resultLog.info("Replication queue {} is blocked.", replicationQueue.getName());

                    // Returns an unmodifiable list of all entries in this queue.
                    if(replicationQueue.entries().isEmpty()) resultLog.debug("Replication queue {} is empty.", replicationQueue.getName());

                } else {
                    resultLog.warn("Agent {} is disabled.", agent.getId());
                }
            } else {
                resultLog.warn("Agent {} is not valid.", agent.getId());
            }
        }

        return new Result(resultLog);
    }
}
