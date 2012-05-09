/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
