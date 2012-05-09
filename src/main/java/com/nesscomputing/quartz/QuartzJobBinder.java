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
