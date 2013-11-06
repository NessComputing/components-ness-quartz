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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.skife.config.TimeSpan;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nesscomputing.config.Config;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.logging.Log;
import com.nesscomputing.quartz.internal.TestingQuartzModule;

public class TestQuartzJob
{
    private static final Log LOG = Log.findLog();
    private List<Module> modules;

    @Before
    public void setup()
    {
        final Config config = Config.getConfig(URI.create("classpath:/test-config"), "quartz");

        modules = Lists.newArrayList();
        modules.add(new NessQuartzModule(config));
        modules.add(new LifecycleModule());
        modules.add(new TestingQuartzModule(config));

        SimpleJob.reset();
    }

    @After
    public void teardown()
    {
        Assert.assertNotNull(modules);
        modules = null;
    }


    @Test
    public void testNoGo() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                QuartzJobBinder.bindQuartzJob(binder(), SimpleJob.class).delay(Duration.standardSeconds(2)).register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(200L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertFalse(SimpleJob.isExecuted());
    }

    @Test
    public void testSimpleJob() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(SimpleJob.class);
                QuartzJobBinder.bindQuartzJob(binder(), SimpleJob.class).delay(Duration.standardSeconds(1)).register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(1500L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(SimpleJob.isExecuted());
    }

    @Test
    public void testStraightUp() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(SimpleJob.class);
                QuartzJobBinder.bindQuartzJob(binder(), SimpleJob.class).register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(200L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(SimpleJob.isExecuted());
    }

    @Test
    public void testRunningJob() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(SimpleJob.class);
                QuartzJobBinder.bindQuartzJob(binder(), SimpleJob.class).conditional("running-job").register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(200L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(SimpleJob.isExecuted());
    }

    @Test
    public void testStoppedJob() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(SimpleJob.class);
                QuartzJobBinder.bindQuartzJob(binder(), SimpleJob.class).conditional("stopped-job").register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(200L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertFalse(SimpleJob.isExecuted());
    }


    @Test
    public void testRepetitive() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(RepetitiveJob.class);
                QuartzJobBinder.bindQuartzJob(binder(), RepetitiveJob.class).repeat(new Duration(100)).register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        RepetitiveJob.reset();

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(2050L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        final int result = RepetitiveJob.getCount();
        Assert.assertEquals(21, result);
    }


    @Test
    public void testGuiceJob() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(GuiceJob.class);
                QuartzJobBinder.bindQuartzJob(binder(), GuiceJob.class).delay(Duration.standardSeconds(1)).register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(1500L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        final Lifecycle lifecycle2 = GuiceJob.getLifecycle();
        Assert.assertNotNull(lifecycle2);
        Assert.assertEquals(lifecycle, lifecycle2);
    }

    @Test
    public void testStartingTime() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(GuiceJob.class);
                DateTime nowPlus2Seconds = DateTime.now().plusSeconds(2);
                QuartzJobBinder.bindQuartzJob(binder(), GuiceJob.class).startTime(nowPlus2Seconds, new TimeSpan("1s")).register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(4000L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        final Lifecycle lifecycle2 = GuiceJob.getLifecycle();
        Assert.assertNotNull(lifecycle2);
        Assert.assertEquals(lifecycle, lifecycle2);
    }

    @Test
    public void testCronJob() throws Exception
    {
        modules.add(new AbstractModule() {
            @Override
            public void configure()
            {
                bind(SimpleJob.class);
                final int targetSecond = DateTime.now().plusSeconds(2).getSecondOfMinute();
                final String cronExpression = String.format("%d * * * * ?", targetSecond);
                QuartzJobBinder.bindQuartzJob(binder(), SimpleJob.class).cronExpression(cronExpression).register();
            }
        });

        Injector injector = Guice.createInjector(modules);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(3000L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(SimpleJob.isExecuted());
    }

    static class SimpleJob implements Job
    {
        private static final AtomicBoolean executed = new AtomicBoolean();

        public SimpleJob()
        {
            SimpleJob.executed.set(false);
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            LOG.info("Simple job executed!");
            SimpleJob.executed.set(true);
        }

        public static boolean isExecuted()
        {
            return executed.get();
        }

        public static void reset()
        {
            executed.set(false);
        }
    }

    static class GuiceJob implements Job
    {
        private static Lifecycle lifecycle2;

        private final Lifecycle lifecycle;

        @Inject
        public GuiceJob(final Lifecycle lifecycle)
        {
            this.lifecycle = lifecycle;
            GuiceJob.lifecycle2 = null;
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            GuiceJob.lifecycle2 = lifecycle;
        }

        public static Lifecycle getLifecycle()
        {
            return lifecycle2;
        }
    }

    static class RepetitiveJob implements Job
    {
        private static int count = 0;

        @Inject
        public RepetitiveJob()
        {
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            RepetitiveJob.count++;
        }

        public static int getCount()
        {
            return count;
        }

        public static void reset()
        {
            RepetitiveJob.count = 0;
        }
    }
}
