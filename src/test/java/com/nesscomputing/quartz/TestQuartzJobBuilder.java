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

import org.apache.commons.configuration.Configuration;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import com.nesscomputing.config.Config;
import com.nesscomputing.quartz.AdHocQuartzJob;

public class TestQuartzJobBuilder extends EasyMockSupport
{
    private Scheduler scheduler = null;
    private Config config = null;
    private Configuration nessJobConfig = null;

    private final Capture<JobDetail> jobDetailCapture = new Capture<JobDetail>(CaptureType.ALL);
    private final Capture<JobDetail> jobAddCapture = new Capture<JobDetail>(CaptureType.ALL);
    private final Capture<Trigger> triggerCapture = new Capture<Trigger>(CaptureType.FIRST);

    @Before
    public void setUp() throws Exception
    {
        config = Config.getConfig(URI.create("classpath:/test-config"), "builder");
        nessJobConfig = config.getConfiguration("ness.job");
        scheduler = createMock(Scheduler.class);

        EasyMock.expect(scheduler.scheduleJob(EasyMock.capture(jobDetailCapture), EasyMock.capture(triggerCapture))).andReturn(null).anyTimes();
        scheduler.addJob(EasyMock.capture(jobAddCapture), EasyMock.anyBoolean());
        EasyMock.expectLastCall().anyTimes();

        replayAll();
    }

    @After
    public void tearDown()
    {
        verifyAll();

        Assert.assertNotNull(config);
        config = null;

        Assert.assertNotNull(nessJobConfig);
        nessJobConfig = null;

        Assert.assertNotNull(scheduler);
        scheduler = null;
    }


    @Test
    public void testSimple() throws Exception
    {
        AdHocQuartzJob.forClass(DummyJob.class).name("test-simple").submitConditional(scheduler, nessJobConfig);

        Assert.assertNotNull(jobDetailCapture.getValues());
        Assert.assertEquals(0, jobAddCapture.getValues().size());
        Assert.assertEquals(1, jobDetailCapture.getValues().size());
        Assert.assertNotNull(jobDetailCapture.getValue());
        Assert.assertEquals(DummyJob.class, jobDetailCapture.getValue().getJobClass());
    }

    @Test
    public void testSkipped() throws Exception
    {
        AdHocQuartzJob.forClass(DummyJob.class).name("testSkipped").conditional("never-run").submitConditional(scheduler, nessJobConfig);

        Assert.assertNotNull(jobDetailCapture.getValues());
        Assert.assertEquals(0, jobDetailCapture.getValues().size());
        Assert.assertEquals(1, jobAddCapture.getValues().size());
        Assert.assertNotNull(jobAddCapture.getValue());
        Assert.assertEquals(DummyJob.class, jobAddCapture.getValue().getJobClass());
    }

    @Test
    public void testRunningLong() throws Exception
    {
        AdHocQuartzJob.forClass(DummyJob.class).name("testRunningLong").conditional("running-long").submitConditional(scheduler, nessJobConfig);

        Assert.assertNotNull(jobDetailCapture.getValues());
        Assert.assertEquals(0, jobAddCapture.getValues().size());
        Assert.assertEquals(1, jobDetailCapture.getValues().size());
        Assert.assertNotNull(jobDetailCapture.getValue());
        Assert.assertEquals(DummyJob.class, jobDetailCapture.getValue().getJobClass());
    }

    @Test
    public void testExplicitSkipped() throws Exception
    {
        AdHocQuartzJob.forClass(DummyJob.class).name("testExplicitSkipped").conditional("ness.job.explicit-never.run.enabled").submitConditional(scheduler, nessJobConfig);

        Assert.assertNotNull(jobDetailCapture.getValues());
        Assert.assertEquals(0, jobDetailCapture.getValues().size());
        Assert.assertEquals(1, jobAddCapture.getValues().size());
        Assert.assertNotNull(jobAddCapture.getValue());
        Assert.assertEquals(DummyJob.class, jobAddCapture.getValue().getJobClass());
    }

    @Test
    public void testExplicitRunning() throws Exception
    {
        AdHocQuartzJob.forClass(DummyJob.class).name("testExplicitRunning").conditional("ness.job.explicit-running.enabled").submitConditional(scheduler, nessJobConfig);

        Assert.assertEquals(0, jobAddCapture.getValues().size());
        Assert.assertNotNull(jobDetailCapture.getValues());
        Assert.assertEquals(1, jobDetailCapture.getValues().size());
        Assert.assertNotNull(jobDetailCapture.getValue());
        Assert.assertEquals(DummyJob.class, jobDetailCapture.getValue().getJobClass());
    }


    public static class DummyJob implements Job
    {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
        }
    }
}




