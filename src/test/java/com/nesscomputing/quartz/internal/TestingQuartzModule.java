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
package com.nesscomputing.quartz.internal;

import org.junit.Ignore;
import org.weakref.jmx.MBeanExporter;

import com.google.inject.AbstractModule;
import com.nesscomputing.config.Config;

@Ignore
public class TestingQuartzModule extends AbstractModule
{
    private final Config config;

    public TestingQuartzModule(final Config config)
    {
        this.config = config;
    }

    @Override
    public void configure()
    {
        binder().disableCircularProxies();
        binder().requireExplicitBindings();

        bind(Config.class).toInstance(config);
        bind(MBeanExporter.class).toInstance(MBeanExporter.withPlatformMBeanServer());
    }
}
