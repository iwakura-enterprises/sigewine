package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import lombok.Getter;

@Bean
@Getter
public class SecondWhoCameService {

    private final PrimordialService primordialService;
    private final LoggingConfiguration loggingConfiguration;

    public SecondWhoCameService(
            PrimordialService primordialService,
            @Bean(name = "debugLogging")
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
