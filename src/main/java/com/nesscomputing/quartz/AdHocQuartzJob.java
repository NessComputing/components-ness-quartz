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

import java.io.Serializable;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public final class AdHocQuartzJob extends QuartzJob<AdHocQuartzJob>
{
    public static AdHocQuartzJob forClass(final Class<? extends Job> jobClass)
    {
        return new AdHocQuartzJob(jobClass);
    }

    AdHocQuartzJob(final Class<? extends Job> jobClass)
    {
        super(jobClass);
    }

    public AdHocQuartzJob addJobData(final String key, final Serializable value)
    {
        super.setJobData(key, value);
        return this;
    }

    public void submit(final Scheduler scheduler)
        throws SchedulerException
    {
        scheduler.scheduleJob(getJobDetail(), getTrigger());
    }
}
