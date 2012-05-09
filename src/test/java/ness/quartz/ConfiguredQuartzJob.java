package ness.quartz;

import org.junit.Ignore;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Singleton;

@Ignore
@Singleton
public class ConfiguredQuartzJob implements Job
{
    private boolean executed = false;

    public ConfiguredQuartzJob()
    {
        executed = false;
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException
    {
        executed = true;
    }

    public boolean isExecuted()
    {
        return executed;
    }
}

