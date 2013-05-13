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

import static com.nesscomputing.quartz.NessQuartzModule.NESS_JOB_NAME;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.skife.config.TimeSpan;

import com.google.inject.name.Named;
import com.nesscomputing.logging.Log;

abstract class QuartzJob<SelfType extends QuartzJob<SelfType>>
{
    private static final Log LOG = Log.findLog();
    private static final Random rand = new Random();

    private final JobDataMap jobDataMap = new JobDataMap();

    private Duration delay = null;
    private Duration repeat = null;
    private String name = "unset-" + UUID.randomUUID().toString();
    private String group = null; // This is the default group in quartz.
    private Boolean enabled = null;
    private String conditional = null;

    private final Class<? extends Job> jobClass;

    protected QuartzJob(final Class<? extends Job> jobClass)
    {
        this.jobClass = jobClass;
    }

    protected void setJobData(final String key, final Serializable value)
    {
        jobDataMap.put(key, value);
    }

    /**
     * Sets a delay until the first time the job is run.
     */
    @SuppressWarnings("unchecked")
    public final SelfType delay(final Duration delay)
    {
        this.delay = delay;
        return (SelfType) this;
    }

    /**
     * Set the time-of-day when the first run of the job will take place.
     */
    @SuppressWarnings("unchecked")
    public final SelfType startTime(final DateTime when, final TimeSpan jitter)
    {
        final int startWeekDay = when.getDayOfWeek();
        final int currentWeekDay = DateTime.now().getDayOfWeek();
        final int daysTilStart = currentWeekDay > startWeekDay ? (startWeekDay + 7 - currentWeekDay) : startWeekDay - currentWeekDay;
        final long millisecondsTilStart = when.getMillisOfDay() + daysTilStart * 24 * 3600 * 1000;
        this.delay = Duration.millis((long)(rand.nextDouble() * jitter.getMillis()) + millisecondsTilStart);
        return (SelfType) this;
    }

    /**
     * Sets a period for the job.
     */
    @SuppressWarnings("unchecked")
    public final SelfType repeat(final Duration repeat)
    {
        this.repeat = repeat;
        return (SelfType) this;
    }

    /**
     * Enabled or disables the job.
     */
    @SuppressWarnings("unchecked")
    public final SelfType enabled(final boolean enabled)
    {
        this.enabled = enabled;
        return (SelfType) this;
    }

    /**
     * Gives a name for a configuration key to check whether the job
     * is enabled or disabled.
     */
    @SuppressWarnings("unchecked")
    public final SelfType conditional(final String conditional)
    {
        this.conditional = conditional;
        return (SelfType) this;
    }

    /**
     * Sets a name for the job.
     */
    @SuppressWarnings("unchecked")
    public final SelfType name(final String name)
    {
        this.name = name;
        return (SelfType) this;
    }

    /**
     * Sets a group for the job.
     */
    @SuppressWarnings("unchecked")
    public final SelfType group(final String group)
    {
        this.group = group;
        return (SelfType) this;
    }

    protected Duration getDelay()
    {
        return delay;
    }

    protected Duration getRepeat()
    {
        return repeat;
    }

    protected String getName()
    {
        return name;
    }

    protected String getGroup()
    {
        return group;
    }

    protected Boolean getEnabled()
    {
        return enabled;
    }

    protected Class<? extends Job> getJobClass()
    {
        return jobClass;
    }

    protected Trigger getTrigger()
    {
        final TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder
            .newTrigger()
            .withIdentity(name, group);

        if (delay != null) {
            triggerBuilder.startAt(new DateTime().plus(delay).toDate());
        }

        if (repeat != null) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(repeat.getMillis()).repeatForever());
        }

        return triggerBuilder.build();
    }

    protected JobDetail getJobDetail()
    {
        final JobBuilder jobBuilder = JobBuilder
            .newJob()
            .ofType(jobClass)
            .storeDurably()
            .usingJobData(jobDataMap);

        if (name != null) {
            jobBuilder.withIdentity(name, group);
        }

        return jobBuilder.build();
    }

    public abstract void submit(final Scheduler scheduler) throws SchedulerException;

    public void submitConditional(final Scheduler scheduler, @Named(NESS_JOB_NAME) final Configuration nessJobConfiguration)
        throws SchedulerException
    {
        String conditionalKey = null;
        final boolean enableJob;

        if (enabled != null) {
            enableJob = enabled;
        }
        else {
            if (conditional == null) {
                enableJob = true;
                LOG.warn("Neither enable nor conditional was set for %s, enabling unconditionally!", name);
            }
            else {
                conditionalKey = StringUtils.removeStart(conditional, NESS_JOB_NAME + ".");
                conditionalKey = StringUtils.endsWith(conditionalKey, ".enabled") ? conditionalKey : conditionalKey + ".enabled";
                enableJob = nessJobConfiguration.getBoolean(conditionalKey, false);
            }
        }

        if (enableJob) {
            submit(scheduler);
        }
        else {
            scheduler.addJob(getJobDetail(), false);
            LOG.info("Job '%s is not scheduled (enabled: %s / conditional: %s)", name, enabled == null ? "<unset>" : enabled.toString(), conditional == null ? "<unset>" : conditionalKey);
        }
    }
}
