package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import lombok.Getter;

@RomaritimeBean
@Getter
public class SecondWhoCameService {

    private final PrimordialService primordialService;
    private final LoggingConfiguration loggingConfiguration;

    public SecondWhoCameService(
            PrimordialService primordialService,
            @RomaritimeBean(name = "debugLogging")
            LoggingConfiguration loggingConfiguration
    ) {
        this.primordialService = primordialService;
        this.loggingConfiguration = loggingConfiguration;
    }

    public void otherStuff() {
        primordialService.doStuff();
        System.out.println("SecondWhoCameService!");
    }
}
