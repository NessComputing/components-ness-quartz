package com.nesscomputing.quartz;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

public class QuartzJobStatistics
{
    private final Timer runtime;

    QuartzJobStatistics(final MetricsRegistry metricsRegistry, final JobKey jobKey)
    {
        final String keyName = jobKey.getName() + (StringUtils.isBlank(jobKey.getGroup()) || JobKey.DEFAULT_GROUP.equals(jobKey.getGroup()) ? "" : "-" + jobKey.getGroup());

        this.runtime = metricsRegistry.newTimer(new MetricName("ness.quartz.job", "statistics", keyName), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
    }

    void registerRuntime(final long nanos)
    {
        runtime.update(nanos, TimeUnit.NANOSECONDS);
    }
}
