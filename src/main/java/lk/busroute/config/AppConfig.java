package lk.busroute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Application-wide configuration.
 * Provides a testable Clock abstraction set to Asia/Colombo timezone.
 */
@Configuration
public class AppConfig {

    public static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    @Bean
    public Clock clock() {
        return Clock.system(SRI_LANKA_ZONE);
    }
}
