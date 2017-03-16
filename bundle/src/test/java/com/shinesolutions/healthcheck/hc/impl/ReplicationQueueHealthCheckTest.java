package com.shinesolutions.healthcheck.hc.impl;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationQueue;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReplicationQueueHealthCheckTest {
    @Mock
    private AgentManager agentManager;

    @InjectMocks
    private HealthCheck healthCheck = new ReplicationQueueHealthCheck();

    private Agent agent1 = mock(Agent.class);
    private Agent agent2 = mock(Agent.class);

    private AgentConfig agentConfig1 = mock(AgentConfig.class);
    private AgentConfig agentConfig2 = mock(AgentConfig.class);

    private ReplicationQueue agent1ReplicationQueue = mock(ReplicationQueue.class);
    private ReplicationQueue agent2ReplicationQueue = mock(ReplicationQueue.class);

    private ReplicationQueue.Status a1rqStatus = mock(ReplicationQueue.Status.class);
    private ReplicationQueue.Status a2rqStatus = mock(ReplicationQueue.Status.class);

    private Map<String, Agent> agents = new HashMap<String, Agent>() {{
        put("Agent 1", agent1);
        put("Agent 2", agent2);
    }};

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);

        long nextRetryTime = 0;

        when(agent1.getId()).thenReturn("Agent 1");
        when(agent1.getConfiguration()).thenReturn(agentConfig1);
        when(agent1.isValid()).thenReturn(true);
        when(agent1.isEnabled()).thenReturn(true);

        when(agent1.getQueue()).thenReturn(agent1ReplicationQueue);
        when(agent1ReplicationQueue.getStatus()).thenReturn(a1rqStatus);
        when(agent1ReplicationQueue.getStatus().getNextRetryTime()).thenReturn(nextRetryTime);

        when(agent2.getId()).thenReturn("Agent 2");
        when(agent2.getConfiguration()).thenReturn(agentConfig2);
        when(agent2.isValid()).thenReturn(true);
        when(agent2.isEnabled()).thenReturn(true);

        when(agent2.getQueue()).thenReturn(agent2ReplicationQueue);
        when(agent2ReplicationQueue.getStatus()).thenReturn(a2rqStatus);
        when(agent2ReplicationQueue.getStatus().getNextRetryTime()).thenReturn(nextRetryTime);

        when(agentManager.getAgents()).thenReturn(agents);
    }

    @Test
    public void testAgentInvalidity() {
        when(agent1.isValid()).thenReturn(false);
        when(agent2.isValid()).thenReturn(false);

        Result result = healthCheck.execute();

        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());
    }

    @Test
    public void testAgentIsDisabled() {

        when(agent1.isEnabled()).thenReturn(false);
        when(agent2.isEnabled()).thenReturn(false);
        Result result = healthCheck.execute();

        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());
    }

    @Test
    public void testBlockedReplicationQueue() {
        long nextRetryTime = 3600;
        when(agent1ReplicationQueue.getStatus().getNextRetryTime()).thenReturn(nextRetryTime);
        Result result = healthCheck.execute();

        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());
        assertTrue("Message should contain DEBUG logs", result.toString().contains("DEBUG"));
    }

    @Test
    public void testExecute() {

        Result result = healthCheck.execute();

        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());
        assertEquals("Message should not contain is not valid", false, result.toString().contains("is not valid."));
    }

    @Test
    public void testNoAgents() {
        when(agentManager.getAgents()).thenReturn(new HashMap<String, Agent>());

        Result result = healthCheck.execute();

        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());
        assertEquals("Message should say no agents are configured", true, result.toString().contains("No agents configured"));
    }
}
