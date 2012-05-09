package ness.quartz.internal;

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
