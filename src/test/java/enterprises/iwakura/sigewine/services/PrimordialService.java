package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import lombok.Getter;

@Getter
@Bean
public class PrimordialService {

    private final LoggingConfiguration loggingConfiguration; // Will be info

    public PrimordialService(@Bean(name = "errorLogging") LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    /**
     * Doing stuff.
     */
    public void doStuff() {
        System.out.println("Doing stuff");
    }
}
