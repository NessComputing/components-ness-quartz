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
