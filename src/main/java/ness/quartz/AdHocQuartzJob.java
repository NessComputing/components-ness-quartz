package ness.quartz;

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
