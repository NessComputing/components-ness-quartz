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

import org.junit.Ignore;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Singleton;

@Ignore
@Singleton
public class ConfiguredQuartzJob implements Job
{
    private boolean executed = false;

    public ConfiguredQuartzJob()
    {
        executed = false;
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException
    {
        executed = true;
    }

    public boolean isExecuted()
    {
        return executed;
    }
}

