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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nesscomputing.config.Config;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.quartz.internal.TestingQuartzModule;

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
