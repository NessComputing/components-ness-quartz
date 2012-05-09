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

import java.net.URI;

import junit.framework.Assert;

import org.joda.time.Duration;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nesscomputing.config.Config;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.quartz.NessQuartzModule;
import com.nesscomputing.quartz.QuartzJobBinder;
import com.nesscomputing.quartz.internal.TestingQuartzModule;


public class TestQuartzGuiceDSL
{
    @Inject
    private Lifecycle lifecycle;

    @Test
    public void testSimpleQuartz() throws Exception
    {
        final Config config = Config.getConfig(URI.create("classpath:/test-config"), "quartz");

        final Injector injector = Guice.createInjector(
            new LifecycleModule(),
            new TestingQuartzModule(config),
            new NessQuartzModule(config),
            new Module() {
                @Override
                public void configure(final Binder binder)
                {
                    binder.bind(SimpleQuartzJob.class);
                    QuartzJobBinder.bindQuartzJob(binder, SimpleQuartzJob.class).delay(Duration.standardSeconds(1)).register();
                }
            });

        injector.injectMembers(this);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(1500L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(SimpleQuartzJob.isExecuted());
    }


    static class SimpleQuartzJob implements Job
    {
        private static boolean executed = false;

        public SimpleQuartzJob()
        {
            SimpleQuartzJob.executed = false;
        }

        @Override
        public void execute(JobExecutionContext arg0) throws JobExecutionException
        {
            SimpleQuartzJob.executed = true;
        }

        public static boolean isExecuted()
        {
            return executed;
        }

    }

}
