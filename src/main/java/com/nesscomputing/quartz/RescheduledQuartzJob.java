package com.nesscomputing.quartz;

import org.joda.time.DateTime;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

public final class RescheduledQuartzJob extends QuartzJob<RescheduledQuartzJob>
{
    private final Trigger trigger;

    public static RescheduledQuartzJob forTrigger(final Trigger trigger)
    {
        return new RescheduledQuartzJob(trigger);
    }

    RescheduledQuartzJob(final Trigger trigger)
    {
        super(null);
        this.trigger = trigger;
    }

    @Override
    protected Trigger getTrigger()
    {
        final TriggerBuilder<? extends Trigger> triggerBuilder = trigger.getTriggerBuilder();
        triggerBuilder.withIdentity((TriggerKey) null);

        if (getDelay() != null) {
            triggerBuilder.startAt(new DateTime().plus(getDelay()).toDate());
        }

        return triggerBuilder.build();
    }

    public void submit(final Scheduler scheduler)
        throws SchedulerException
    {
        scheduler.rescheduleJob(trigger.getKey(), getTrigger());
    }
}
