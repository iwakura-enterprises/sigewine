package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import org.slf4j.event.Level;

/**
 * Test configuration class for logging.
 */
public class TestLoggingConfig {

    @Bean
    public LoggingConfiguration defaultLoggingConfig() {
        return new LoggingConfiguration(Level.INFO);
    }

    @Bean(name = "errorLogging")
    public LoggingConfiguration errorLoggingConfig() {
        return new LoggingConfiguration(Level.ERROR);
    }

    @Bean(name = "debugLogging")
    public LoggingConfiguration debugLoggingConfig() {
        return new LoggingConfiguration(Level.DEBUG);
    }
}
