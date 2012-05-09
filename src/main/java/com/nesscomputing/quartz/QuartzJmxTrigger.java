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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.weakref.jmx.JmxException;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.Managed;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleListener;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.logging.Log;

public class QuartzJmxTrigger
{
    private static final Log LOG = Log.findLog();

    private final Set<String> keys = Sets.newHashSet();

    private final Scheduler scheduler;

    private MBeanExporter mbeanExporter = null;
    private Set<QuartzJobBinder> jobs = null;

    @Inject
    QuartzJmxTrigger(final Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    @Inject(optional=true)
    void setMBeanExporter(final MBeanExporter mbeanExporter,
                                 final Set<QuartzJobBinder> jobs,
                                 final Lifecycle lifecycle)
    {
        this.mbeanExporter = mbeanExporter;
        this.jobs = jobs;

        lifecycle.addListener(LifecycleStage.START_STAGE, new LifecycleListener() {

            @Override
            public void onStage(final LifecycleStage lifecycleStage) {
                export();
            }
        });

        lifecycle.addListener(LifecycleStage.STOP_STAGE, new LifecycleListener() {

            @Override
            public void onStage(final LifecycleStage lifecycleStage) {
                unexport();
            }
        });
    }

    void export()
    {
        if (mbeanExporter == null || jobs == null || jobs.isEmpty()) {
            return;
        }

        for (QuartzJobBinder job : jobs) {
            final String keyName = "ness.quartz.job:type=trigger, name=" + job.getName() + (StringUtils.isBlank(job.getGroup()) ? "" : "-" + job.getGroup());
            keys.add(keyName);
            mbeanExporter.export(keyName, new QuartzJmxWrapper(job));
        }
    }

    void unexport()
    {
        if (mbeanExporter == null) {
            return;
        }

        for (String key : keys) {
            try {
                mbeanExporter.unexport(key);
            }
            catch (JmxException je) {
                switch (je.getReason()) {
                    case INSTANCE_NOT_FOUND:
                        LOG.trace("Instance %s does not exist", key);
                        break;
                    default:
                        LOG.warn("While unexporting %s: %s", key, je.getCause());
                        break;
                }
            }
        }
    }

    public class QuartzJmxWrapper
    {
        final QuartzJobBinder job;
        final JobKey jobKey;

        private QuartzJmxWrapper(final QuartzJobBinder job)
        {
            this.job = job;
            this.jobKey = job.getJobDetail().getKey();
        }

        @Managed
        public String getJobKey()
        {
            return jobKey.toString();
        }

        @Managed
        public boolean isExisting()
            throws SchedulerException
        {
            return scheduler.checkExists(jobKey);
        }

        @Managed
        public String [] getTriggerState()
            throws SchedulerException
        {
            final List<? extends Trigger> triggers =  scheduler.getTriggersOfJob(jobKey);
            final String [] triggerStates = new String [triggers.size()];
            for (int i = 0; i < triggerStates.length; i++) {
                final TriggerKey triggerKey = triggers.get(i).getKey();
                triggerStates[i] = triggerKey.toString() + ": " + scheduler.getTriggerState(triggers.get(i).getKey()).toString();
            }
            return triggerStates;
        }

        @Managed
        public boolean isRunning()
            throws SchedulerException
        {
            final List<JobExecutionContext> jobExecutionContexts = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext jobContext : jobExecutionContexts) {
                if (jobContext.getJobDetail().getKey().equals(jobKey)) {
                    return true;
                }
            }
            return false;
        }

        @Managed
        public void trigger()
            throws SchedulerException
        {
            scheduler.triggerJob(jobKey);
        }

        @Managed
        public void pauseScheduling()
            throws SchedulerException
        {
            scheduler.pauseJob(jobKey);
        }

        @Managed
        public void resumeScheduling()
            throws SchedulerException
        {
            scheduler.resumeJob(jobKey);
        }

        @Managed
        public void cancel()
            throws SchedulerException
        {
            scheduler.interrupt(jobKey);
        }
    }
}

