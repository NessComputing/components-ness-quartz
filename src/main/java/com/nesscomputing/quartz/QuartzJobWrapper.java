package com.nesscomputing.quartz;

import static java.lang.String.format;

import javax.annotation.Nonnull;

import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import com.google.common.base.Preconditions;

public class QuartzJobWrapper implements InterruptableJob
{
    private final Job wrappedJob;
    private final QuartzJobStatistics stats;

    QuartzJobWrapper(@Nonnull final Job wrappedJob, @Nonnull final QuartzJobStatistics stats)
    {
        Preconditions.checkNotNull(wrappedJob);
        Preconditions.checkNotNull(stats);
        this.wrappedJob = wrappedJob;
        this.stats = stats;
    }

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException
    {
        final long startTime = System.nanoTime();

        try {
            wrappedJob.execute(context);
        }
        finally {
            final long runtime = System.nanoTime() - startTime;
            stats.registerRuntime(runtime);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException
    {
        if (!(wrappedJob instanceof InterruptableJob)) {
            throw new UnableToInterruptJobException(format("Job '%s' is not interruptable!", wrappedJob.getClass().getSimpleName()));
        }
        else {
            ((InterruptableJob) wrappedJob).interrupt();
        }
    }
}
