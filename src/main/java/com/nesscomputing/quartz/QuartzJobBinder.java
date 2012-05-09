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

import static java.lang.String.format;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

public class QuartzJobBinder extends QuartzJob<QuartzJobBinder>
{
    public static QuartzJobBinder bindQuartzJob(final Binder binder, final Class<? extends Job> jobClass)
    {
        return new QuartzJobBinder(binder, jobClass);
    }

    private final Binder binder;

    QuartzJobBinder(final Binder binder, final Class<? extends Job> jobClass)
    {
        super(jobClass);
        this.binder = binder;
    }

    public void register()
    {
        final Multibinder<QuartzJobBinder> jobs = Multibinder.newSetBinder(binder, QuartzJobBinder.class);
        jobs.addBinding().toInstance(this);
    }

    public final void submit(final Scheduler scheduler)
        throws SchedulerException
    {
        scheduler.scheduleJob(getJobDetail(), getTrigger());
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(format("job(%s): class=%s", getName(), getJobClass().getSimpleName()));

        if (getGroup() != null) {
            sb.append(format(", group=%s",getGroup()));
        }
        if (getDelay() != null) {
            sb.append(format(", delay=%s", printDuration(getDelay())));
        }
        if (getRepeat() != null) {
            sb.append(format(", repeat=%s",  printDuration(getRepeat())));
        }
        if (getEnabled() != null) {
            sb.append(format(", enabled=%s", getEnabled()));
        }
        return sb.toString();
    }

    private String printDuration(final Duration duration)
    {
        return PeriodFormat.getDefault().print(duration.toPeriod());
    }
}
