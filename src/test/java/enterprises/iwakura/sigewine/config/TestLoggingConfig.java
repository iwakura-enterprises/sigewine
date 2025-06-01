package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import org.slf4j.event.Level;

/**
 * Test configuration class for logging.
 */
public class TestLoggingConfig {

    @RomaritimeBean
    public LoggingConfiguration defaultLoggingConfig() {
        return new LoggingConfiguration(Level.INFO);
    }

    @RomaritimeBean(name = "errorLogging")
    public LoggingConfiguration errorLoggingConfig() {
        return new LoggingConfiguration(Level.ERROR);
    }

    @RomaritimeBean(name = "debugLogging")
    public LoggingConfiguration debugLoggingConfig() {
        return new LoggingConfiguration(Level.DEBUG);
    }
}
