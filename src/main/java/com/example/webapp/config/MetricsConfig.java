package com.example.webapp.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class MetricsConfig {

    @Value("${metrics.statsd.enabled:true}")
    private boolean statsdEnabled;

    @Value("${metrics.statsd.host:localhost}")
    private String statsdHost;

    @Value("${metrics.statsd.port:8125}")
    private int statsdPort;

    @Bean
    public MeterRegistry meterRegistry() {
        if (statsdEnabled) {
            StatsdConfig config = new StatsdConfig() {
                @Override
                public String get(String key) {
                    return null;
                }

                @Override
                public String prefix() {
                    return "webapp";
                }

                @Override
                public StatsdFlavor flavor() {
                    return StatsdFlavor.DATADOG;
                }

                @Override
                public String host() {
                    return statsdHost;
                }

                @Override
                public int port() {
                    return statsdPort;
                }
            };

            return new StatsdMeterRegistry(config, io.micrometer.core.instrument.Clock.SYSTEM);
        }
        return null;
    }
}