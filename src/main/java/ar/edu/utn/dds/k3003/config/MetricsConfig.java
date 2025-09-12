package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean TimedAspect timedAspect(MeterRegistry registry){ return new TimedAspect(registry); }
    @Bean ObservationRegistry observationRegistry(){ return ObservationRegistry.create(); }
}
