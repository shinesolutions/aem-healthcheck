package com.shinesolutions.healthcheck.hc.impl;

import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.Statistics;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlingJobsHealthCheckTest {

    @Mock
    private JobManager jobManager;

    @InjectMocks
    private HealthCheck healthCheck = new SlingJobsHealthCheck();

    private Statistics statistics = mock(Statistics.class);

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);

        long activeJobs = 5;
        long cancelledJobs = 2;
        long failedJobs = 1;

        when(jobManager.getStatistics()).thenReturn(statistics);
        when(statistics.getNumberOfActiveJobs()).thenReturn(activeJobs);
        when(statistics.getNumberOfCancelledJobs()).thenReturn(cancelledJobs);
        when(statistics.getNumberOfFailedJobs()).thenReturn(failedJobs);
    }

    @Test
    public void testEmptyCurrentQueues(){
        when(jobManager.getQueues()).thenReturn(null);
        Result result = healthCheck.execute();
        String msg = "There are currently no queues available.";
        assertTrue("Message should say no queues available", result.toString().contains(msg));
    }

    @Test
    public void testEmptyTotalJobs() {
        long totalJobs = 0;
        when(statistics.getNumberOfJobs()).thenReturn(totalJobs);
        Result result = healthCheck.execute();
        String msg = "Found no jobs in the Job Manager.";
        assertTrue("Message should say no jobs available", result.toString().contains(msg));
    }

    @Test
    public void testTotalJobs() {
        long totalJobs = 10;
        when(statistics.getNumberOfJobs()).thenReturn(totalJobs);
        Result result = healthCheck.execute();
        String msg = "Found 10 total jobs.";
        assertTrue("Message should say it found jobs available", result.toString().contains(msg));
    }

    @Test
    public void testNoQueuedJobs() {
        long queuedJobs = 0;
        when(statistics.getNumberOfQueuedJobs()).thenReturn(queuedJobs);
        Result result = healthCheck.execute();
        String msg = "Found no queued jobs.";
        assertTrue("Message should say no jobs are queued", result.toString().contains(msg));
    }

    @Test
    public void testQueuedJobs() {
        long queuedJobs = 10;
        when(statistics.getNumberOfQueuedJobs()).thenReturn(queuedJobs);
        Result result = healthCheck.execute();
        String msg = "Found 10 queued jobs.";
        assertTrue("Message should say it found queued jobs", result.toString().contains(msg));
    }

    @Test
    public void testQueuedJobsExceedThreshold() {
        long queuedJobs = 1200;
        when(statistics.getNumberOfQueuedJobs()).thenReturn(queuedJobs);
        Result result = healthCheck.execute();
        String msg = "Found 1200 queued jobs.";
        assertTrue("Message should say it found queued jobs", result.toString().contains(msg));
        assertEquals("Result should not be ok", false, result.isOk());
        assertEquals("Status should not be OK", Result.Status.WARN, result.getStatus());
    }

    @Test
    public void testNoActiveJobs() {
        long activeJobs = 0;
        when(statistics.getNumberOfActiveJobs()).thenReturn(activeJobs);
        Result result = healthCheck.execute();
        String msg = "Found no active jobs.";
        assertTrue("Message should say no jobs are active", result.toString().contains(msg));
    }

    @Test
    public void testActiveJobs() {
        long activeJobs = 10;
        when(statistics.getNumberOfActiveJobs()).thenReturn(activeJobs);
        Result result = healthCheck.execute();
        String msg = "Found 10 active jobs.";
        assertTrue("Message should say it found active jobs", result.toString().contains(msg));
    }

    @Test
    public void testNoCancelledJobs() {
        long cancelledJobs = 0;
        when(statistics.getNumberOfCancelledJobs()).thenReturn(cancelledJobs);
        Result result = healthCheck.execute();
        String msg = "Found no cancelled jobs.";
        assertTrue("Message should say no jobs are cancelled", result.toString().contains(msg));
    }

    @Test
    public void testCancelledJobs() {
        long cancelledJobs = 10;
        when(statistics.getNumberOfCancelledJobs()).thenReturn(cancelledJobs);
        Result result = healthCheck.execute();
        String msg = "Found 10 cancelled jobs.";
        assertTrue("Message should say it found cancelled jobs", result.toString().contains(msg));
    }

    @Test
    public void testNoFailedJobs() {
        long failedJobs = 0;
        when(statistics.getNumberOfFailedJobs()).thenReturn(failedJobs);
        Result result = healthCheck.execute();
        String msg = "Found no failed jobs.";
        assertTrue("Message should say no jobs have failed", result.toString().contains(msg));
    }

    @Test
    public void testFailedJobs() {
        long failedJobs = 10;
        when(statistics.getNumberOfFailedJobs()).thenReturn(failedJobs);
        Result result = healthCheck.execute();
        String msg = "Found 10 failed jobs.";
        assertTrue("Message should say it found failed jobs", result.toString().contains(msg));
    }

    @Test
    public void testAverageProcessingTime() {
        long averageProcessingTime = 3600;
        when(statistics.getAverageProcessingTime()).thenReturn(averageProcessingTime);
        Result result = healthCheck.execute();
        String msg = "The average processing time is ";
        assertTrue("Message should inform of average processing time", result.toString().contains(msg));
    }

    @Test
    public void testEmptyAverageProcessingTime() {
        long averageProcessingTime = 0;
        when(statistics.getAverageProcessingTime()).thenReturn(averageProcessingTime);
        Result result = healthCheck.execute();
        String msg = "The average processing time is ";
        assertFalse("Message should not inform of average processing time", result.toString().contains(msg));
    }

    @Test
    public void testAverageWaitingTime() {
        long averageWaitingTime = 3600;
        when(statistics.getAverageWaitingTime()).thenReturn(averageWaitingTime);
        Result result = healthCheck.execute();
        String msg = "The average waiting time is ";
        assertTrue("Message should inform of average waiting time", result.toString().contains(msg));
    }

    @Test
    public void testEmptyAverageWaitingTime() {
        long averageWaitingTime = 0;
        when(statistics.getAverageWaitingTime()).thenReturn(averageWaitingTime);
        Result result = healthCheck.execute();
        String msg = "The average waiting time is ";
        assertFalse("Message should not inform of average waiting time", result.toString().contains(msg));
    }

    @Test
    public void testExecute() {
        Result result = healthCheck.execute();
        assertEquals("Status should be OK", Result.Status.OK, result.getStatus());
        assertEquals("Result should be ok", true, result.isOk());
    }
}
