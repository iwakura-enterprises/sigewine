package enterprises.iwakura.sigewine;

import enterprises.iwakura.sigewine.beans.BeanizedBean;
import enterprises.iwakura.sigewine.services.TeyvatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

public class SigewineTest {

    @Test
    public void run_defaultOptions() {
        // Arrange
        //@formatter:off
        SigewineOptions sigewineOptions = SigewineOptions.builder()
            .logLevel(Level.INFO)
            .build();
        Sigewine sigewine = new Sigewine(sigewineOptions);

        // Act
        sigewine.treatment(SigewineTest.class);
        final var teyvatService = sigewine.syringe(TeyvatService.class);
        final var beanizedBean = sigewine.syringe(BeanizedBean.class);

        // Assert
        Assertions.assertNotNull(teyvatService, "Teyvat Service should not be null");
        Assertions.assertInstanceOf(TeyvatService.class, teyvatService, "Teyvat Service should be an instance of TeyvatService");
        Assertions.assertEquals(3, teyvatService.getEntities().size(), "There should be 3 entities in TeyvatService#entities");

        final var teyvatLogging = teyvatService.getLoggingConfiguration();
        Assertions.assertNotNull(teyvatLogging, "Teyvat Logging should not be null");
        Assertions.assertEquals(Level.INFO, teyvatLogging.getLogLevel(), "Teyvat Logging should be INFO");

        final var secondWhoCameService = teyvatService.getSecondWhoCameService();
        Assertions.assertNotNull(secondWhoCameService, "Second Who Came Service should not be null");
        Assertions.assertNotNull(secondWhoCameService.getPrimordialService(), "Primordial Service should not be null");
        Assertions.assertNotNull(secondWhoCameService.getLoggingConfiguration(), "Second Who Came Service Logging should not be null");
        Assertions.assertEquals(Level.DEBUG, secondWhoCameService.getLoggingConfiguration().getLogLevel(), "SecondWhoCameService Logging should be DEBUG");

        final var primordialService = secondWhoCameService.getPrimordialService();
        Assertions.assertNotNull(primordialService, "Primordial Service should not be null");
        Assertions.assertNotNull(primordialService.getLoggingConfiguration(), "Primordial Service Logging should not be null");
        Assertions.assertEquals(Level.ERROR, primordialService.getLoggingConfiguration().getLogLevel(), "PrimordialService Logging should be DEBUG");

        final var teyvatPrimordialService = teyvatService.getPrimordialService();
        Assertions.assertNotNull(teyvatPrimordialService, "Teyvat Primordial Service should not be null");
        Assertions.assertNotNull(teyvatPrimordialService.getLoggingConfiguration(), "Teyvat Primordial Service Logging should not be null");
        Assertions.assertEquals(Level.ERROR, teyvatPrimordialService.getLoggingConfiguration().getLogLevel(), "Teyvat Primordial Service Logging should be WARN");
        Assertions.assertEquals(teyvatPrimordialService, primordialService, "Teyvat Primordial Service should be the same as Primordial Service");

        Assertions.assertNotNull(beanizedBean, "BeanizedBean should not be null");
        final var beanizedBeanLogLevel = beanizedBean.logLevel;
        Assertions.assertEquals(Level.ERROR, beanizedBeanLogLevel, "BeanizedBean log level should be ERROR");
        //@formatter:on
    }
}
