package ness.quartz;

import java.net.URI;

import junit.framework.Assert;
import ness.quartz.internal.TestingQuartzModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nesscomputing.config.Config;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;

public class TestQuartz
{
    private Injector injector = null;

    @Before
    public void setup()
    {
        final Config config = Config.getConfig(URI.create("classpath:/test-config"), "quartz");

        injector = Guice.createInjector(
                        new NessQuartzModule(config),
                        new LifecycleModule(),
                        new TestingQuartzModule(config)
        );
    }

    @After
    public void teardown()
    {
        Assert.assertNotNull(injector);
        injector = null;
    }


    @Test
    public void testSimple() throws Exception
    {
        final Scheduler scheduler = injector.getInstance(Scheduler.class);
        Assert.assertNotNull(scheduler);
        Assert.assertFalse(scheduler.isStarted());
    }

    @Test
    public void testStartStop() throws Exception
    {
        final Scheduler scheduler = injector.getInstance(Scheduler.class);
        Assert.assertNotNull(scheduler);

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        Assert.assertFalse(scheduler.isStarted());
        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(200L);
        Assert.assertTrue(scheduler.isStarted());

        Assert.assertFalse(scheduler.isShutdown());
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        Assert.assertTrue(scheduler.isShutdown());
    }
}
