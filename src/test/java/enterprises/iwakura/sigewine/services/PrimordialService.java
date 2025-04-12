package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import lombok.*;

@Getter
@RomaritimeBean
public class PrimordialService {

    private final LoggingConfiguration loggingConfiguration; // Will be info

    public PrimordialService(@RomaritimeBean(name = "errorLogging") LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    /**
     * Doing stuff.
     */
    public void doStuff() {
        System.out.println("Doing stuff");
    }
}
