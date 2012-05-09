package ness.quartz;

import java.util.concurrent.ConcurrentMap;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * Implementation of a Quartz JobFactory using Guice to get the jobs to run. This allows for injection on
 * job classes.
 */
public class GuiceJobFactory implements JobFactory
{
    private static final ConcurrentMap<JobKey, QuartzJobStatistics> jobStatistics = Maps.newConcurrentMap();

    private final Injector injector;

    private MetricsRegistry metricsRegistry = null;

    @Inject
    GuiceJobFactory(final Injector injector)
    {
        this.injector = injector;
    }

    @Inject(optional=true)
    void injectMetricsRegistry(final MetricsRegistry metricsRegistry)
    {
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException
    {
        final JobDetail jobDetail = bundle.getJobDetail();

        final Class<? extends Job> jobClass = jobDetail.getJobClass();
        final Job job =  injector.getInstance(jobClass);

        if (metricsRegistry == null) {
            return job;
        }

        final JobKey jobKey = jobDetail.getKey();
        QuartzJobStatistics stats = jobStatistics.get(jobKey);
        if (stats == null) {
            stats = new QuartzJobStatistics(metricsRegistry, jobKey);
            final QuartzJobStatistics newStats = jobStatistics.putIfAbsent(jobKey, stats);
            stats = Objects.firstNonNull(newStats, stats);
        }

        return new QuartzJobWrapper(job, stats);
    }
}
