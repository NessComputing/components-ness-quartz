package ness.quartz;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.spi.JobFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.name.Named;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.AbstractLifecycleProvider;
import com.nesscomputing.lifecycle.guice.LifecycleAction;
import com.nesscomputing.logging.Log;

public class SchedulerProvider extends AbstractLifecycleProvider<Scheduler> implements Provider<Scheduler>
{
    public static final Log LOG = Log.findLog();

    private final SchedulerFactory schedulerFactory;
    private final JobFactory jobFactory;
    private final Configuration nessJobConfig;

    private Set<QuartzJobBinder> jobs = null;

    @Inject
    public SchedulerProvider(final SchedulerFactory schedulerFactory,
                             final JobFactory jobFactory,
                             final NessQuartzConfig nessQuartzConfig,
                             @Named("ness.job") final Configuration nessJobConfig)
    {
        this.schedulerFactory = schedulerFactory;
        this.jobFactory = jobFactory;
        this.nessJobConfig = nessJobConfig;

        addAction(LifecycleStage.START_STAGE, new LifecycleAction<Scheduler>() {
                @Override
                public void performAction(final Scheduler scheduler) {
                    try {
                        scheduler.startDelayed((int) (nessQuartzConfig.getStartDelay().getMillis() / 1000L));
                        LOG.info("Quartz Scheduler started!");
                    }
                    catch (SchedulerException se) {
                        LOG.error(se, "Could not start Quartz Scheduler");
                    }
                }
            });

        addAction(LifecycleStage.STOP_STAGE, new LifecycleAction<Scheduler>() {
                @Override
                public void performAction(final Scheduler scheduler) {
                    try {
                        scheduler.shutdown(nessQuartzConfig.isShutdownWaitForJobs());
                        LOG.info("Quartz Scheduler stopped.");
                    }
                    catch (SchedulerException se) {
                        LOG.error(se, "Could not stop Quartz Scheduler");
                    }
                }
            });

    }

    /**
     * Get the list of Jobs from the Multibinder. If no jobs have been bound, this
     * method is not called.
     */
    @Inject(optional=true)
    public void setJobs(final Set<QuartzJobBinder> jobs)
    {
        this.jobs = jobs;
    }

    @Override
    public Scheduler internalGet()
    {
        try {
            final Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.setJobFactory(jobFactory);

            if (jobs != null) {
                for (final QuartzJobBinder job : jobs) {
                    job.submitConditional(scheduler, nessJobConfig);
                }
            }
            return scheduler;
        }
        catch (SchedulerException se) {
            throw new ProvisionException("Could not instantiate Quartz Scheduler!", se);
        }
    }
}
