package com.nesscomputing.quartz;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.nesscomputing.config.Config;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.quartz.internal.TestingQuartzModule;


public class TestConfiguredJob
{
    @Inject
    private Lifecycle lifecycle;

    @Inject
    private ConfiguredQuartzJob configuredQuartzJob;

    @Test
    public void testSimpleQuartz() throws Exception
    {
        final Config config = Config.getConfig(URI.create("classpath:/test-config"), "configured");

        final Injector injector = Guice.createInjector(
                                                       new LifecycleModule(),
                                                       new TestingQuartzModule(config),
                                                       new NessQuartzModule(config));

        injector.injectMembers(this);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        Thread.sleep(1500L);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(configuredQuartzJob.isExecuted());
    }
}
