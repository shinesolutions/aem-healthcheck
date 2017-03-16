package com.shinesolutions.healthcheck.hc.impl;

import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.Queue;
import org.apache.sling.event.jobs.Statistics;
import org.apache.sling.hc.annotations.SlingHealthCheck;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.util.FormattingResultLog;


/**
 * Health Check to test the number of active jobs in the sling queue.
 */
@SlingHealthCheck(
        name = "Sling Jobs Health Check",
        mbeanName = "slingJobsHC",
        description = "This health check tests the number of active jobs and their general health in the queue.",
        tags = {"deep"}
)
public class SlingJobsHealthCheck implements HealthCheck {

    @Reference
    private JobManager jobManager;

    private static final int MAX_JOBS_QUEUED = 1000;

    @Override
    public Result execute() {
        FormattingResultLog resultLog = new FormattingResultLog();

        if(jobManager != null) {
            // Get current queues in job manager
            Iterable<Queue> queues = jobManager.getQueues();
            if(queues != null) {
                for (Queue queue : queues) {
                    String name = queue.getName();
                    String info = queue.getStateInfo();
                    resultLog.info("The queue {} is currently {}", name, info);
                }
            } else {
                resultLog.info("There are currently no queues available.");
            }

            // Get general statistics for the Job Manager
            Statistics statistics = jobManager.getStatistics();

            long totalJobs = statistics.getNumberOfJobs();
            if (totalJobs > 0)
                resultLog.info("Found {} total jobs.", totalJobs); else resultLog.debug("Found no jobs in the Job Manager.");

            long queuedJobs = statistics.getNumberOfQueuedJobs();
            if(queuedJobs > 0)
                resultLog.info("Found {} queued jobs.", queuedJobs); else resultLog.debug("Found no queued jobs.");

            long activeJobs = statistics.getNumberOfActiveJobs();
            if(activeJobs > 0)
                resultLog.info("Found {} active jobs.", activeJobs); else resultLog.debug("Found no active jobs.");

            long cancelledJobs = statistics.getNumberOfCancelledJobs();
            if(cancelledJobs > 0)
                resultLog.info("Found {} cancelled jobs.", cancelledJobs); else resultLog.debug("Found no cancelled jobs.");

            long failedJobs = statistics.getNumberOfFailedJobs();
            if(failedJobs > 0)
                resultLog.info("Found {} failed jobs.", failedJobs); else resultLog.debug("Found no failed jobs.");

            long averageProcessingTime = statistics.getAverageProcessingTime();
            if(averageProcessingTime > 0)
                resultLog.debug("The average processing time is {}.", averageProcessingTime);

            long averageWaitingTime = statistics.getAverageWaitingTime();
            if(averageWaitingTime > 0)
                resultLog.debug("The average waiting time is [{}.", averageWaitingTime);

            if(queuedJobs > MAX_JOBS_QUEUED) {
                resultLog.warn("Found more than {} jobs queued: {}", MAX_JOBS_QUEUED, queuedJobs);
            }
        } else {
            resultLog.info("No Job Manager available");
        }
        return new Result(resultLog);
    }
}
