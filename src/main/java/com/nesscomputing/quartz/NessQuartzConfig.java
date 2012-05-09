package com.nesscomputing.quartz;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.TimeSpan;

public abstract class NessQuartzConfig
{
    @Config("ness.quartz.start-delay")
    @Default("0s")
    public TimeSpan getStartDelay()
    {
        return new TimeSpan("0s");
    }

    @Config("ness.quartz.shutdown-wait-for-jobs")
    @Default("false")
    public boolean isShutdownWaitForJobs()
    {
        return false;
    }
}
