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
