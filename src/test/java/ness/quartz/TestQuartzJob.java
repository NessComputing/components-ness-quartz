package ness.quartz;

import java.net.URI;
import java.util.List;

import junit.framework.Assert;
import ness.quartz.internal.TestingQuartzModule;

import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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

public class TestQuartzJob
{
    private List<Module> modules;

    @Before
    public void setup()
    {
        final Config config = Config.getConfig(URI.create("classpath:/test-config"), "quartz");

        modules = Lists.newArrayList();
        modules.add(new NessQuartzModule(config));
        modules.add(new LifecycleModule());
        modules.add(new TestingQuartzModule(config));
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

        Assert.assertTrue(SimpleJob.isExecuted());
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

    static class SimpleJob implements Job
    {
        private static boolean executed = false;

        public SimpleJob()
        {
            SimpleJob.executed = false;
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            SimpleJob.executed = true;
        }

        public static boolean isExecuted()
        {
            return executed;
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
