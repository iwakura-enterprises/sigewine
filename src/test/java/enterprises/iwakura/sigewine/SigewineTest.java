package enterprises.iwakura.sigewine;

import enterprises.iwakura.sigewine.annotations.ClassWrappedMethodWrapper;
import enterprises.iwakura.sigewine.annotations.OtherAnnotationMethodWrapper;
import enterprises.iwakura.sigewine.annotations.TransactionalMethodWrapper;
import enterprises.iwakura.sigewine.aop.extension.AopConstellation;
import enterprises.iwakura.sigewine.beans.BeanizedBean;
import enterprises.iwakura.sigewine.core.*;
import enterprises.iwakura.sigewine.services.DatabaseServerImpl;
import enterprises.iwakura.sigewine.services.TeyvatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

@Slf4j
public class SigewineTest {

    @Test
    public void run_defaultOptions() {
        // Arrange
        //@formatter:off
        SigewineOptions sigewineOptions = SigewineOptions.builder()
            .build();
        Sigewine sigewine = new Sigewine(sigewineOptions);
        AopConstellation aopConstellation = new AopConstellation(1);
        aopConstellation.addMethodWrapper(new TransactionalMethodWrapper());
        aopConstellation.addMethodWrapper(new OtherAnnotationMethodWrapper());
        aopConstellation.addMethodWrapper(new ClassWrappedMethodWrapper());
        sigewine.addConstellation(aopConstellation);

        // Act
        sigewine.treatment(SigewineTest.class);
        final var teyvatService = sigewine.syringe(TeyvatService.class);
        final var beanizedBean = sigewine.syringe(BeanizedBean.class);

        // Assert
        Assertions.assertNotNull(teyvatService, "Teyvat Service should not be null");
        Assertions.assertInstanceOf(TeyvatService.class, teyvatService, "Teyvat Service should be an instance of TeyvatService");
        Assertions.assertEquals(3, teyvatService.getEntities().size(), "There should be 3 entities in TeyvatService#entities");
        Assertions.assertNotNull(teyvatService.getDatabaseService(), "Teyvat Service Database Service should not be null");
        Assertions.assertInstanceOf(DatabaseServerImpl.class, teyvatService.getDatabaseService(), "Teyvat Service Database Service should be an instance of DatabaseServerImpl");

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

        int ranTimes = ClassWrappedMethodWrapper.ranTimes;
        teyvatService.someUnannotatedMethod();
        Assertions.assertFalse(OtherAnnotationMethodWrapper.ran);
        Assertions.assertFalse(TransactionalMethodWrapper.ran);
        teyvatService.someAnnotatedMethod();
        Assertions.assertTrue(OtherAnnotationMethodWrapper.ran);
        Assertions.assertTrue(TransactionalMethodWrapper.ran);
        Assertions.assertEquals(ranTimes + 2, ClassWrappedMethodWrapper.ranTimes);
    }
}
