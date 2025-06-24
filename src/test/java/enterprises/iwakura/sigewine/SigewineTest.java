package enterprises.iwakura.sigewine;

import enterprises.iwakura.sigewine.annotations.ClassWrappedMethodWrapper;
import enterprises.iwakura.sigewine.annotations.OtherAnnotationMethodWrapper;
import enterprises.iwakura.sigewine.annotations.TransactionalMethodWrapper;
import enterprises.iwakura.sigewine.aop.extension.AopConstellation;
import enterprises.iwakura.sigewine.aop.sentry.SentryTransactionMethodWrapper;
import enterprises.iwakura.sigewine.beans.BeanizedBean;
import enterprises.iwakura.sigewine.core.*;
import enterprises.iwakura.sigewine.services.DatabaseServerImpl;
import enterprises.iwakura.sigewine.services.ImplSelfInjectedBean;
import enterprises.iwakura.sigewine.services.TeyvatService;
import enterprises.iwakura.sigewine.services.sentry.SentryServiceThree;
import io.sentry.Sentry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

@Slf4j
public class SigewineTest {

    @BeforeEach
    public void reset() {
        // Reset static fields before each test
        ClassWrappedMethodWrapper.ranTimes = 0;
        OtherAnnotationMethodWrapper.ran = false;
        TransactionalMethodWrapper.ran = false;
    }

    @Test
    @SneakyThrows
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
        aopConstellation.addMethodWrapper(new SentryTransactionMethodWrapper());
        sigewine.addConstellation(aopConstellation);

        Sentry.init(options -> {
            options.setEnabled(false); // Disable Sentry for this test
            options.setDebug(true);
            options.setTracesSampleRate(1.0);
        });

        // Act
        sigewine.treatment(SigewineTest.class);
        final var teyvatService = sigewine.syringe(TeyvatService.class);
        final var beanizedBean = sigewine.syringe(BeanizedBean.class);
        final var selfInjectedBean = sigewine.syringe(ImplSelfInjectedBean.class);
        final var serviceThree = sigewine.syringe(SentryServiceThree.class);

        // Assert
        Assertions.assertNotNull(teyvatService, "Teyvat Service should not be null");
        Assertions.assertInstanceOf(TeyvatService.class, teyvatService, "Teyvat Service should be an instance of TeyvatService");
        Assertions.assertEquals(3, teyvatService.getEntities().size(), "There should be 3 entities in TeyvatService#entities");
        Assertions.assertNotNull(teyvatService.getDatabaseService(), "Teyvat Service Database Service should not be null");
        Assertions.assertInstanceOf(DatabaseServerImpl.class, teyvatService.getDatabaseService(), "Teyvat Service Database Service should be an instance of DatabaseServerImpl");
        Assertions.assertNotNull(teyvatService.getSelf());
        Assertions.assertInstanceOf(TeyvatService.class, teyvatService.getSelf());

        Assertions.assertNotNull(selfInjectedBean);
        Assertions.assertNotNull(selfInjectedBean.getSelfInjectedBaseClass());
        Assertions.assertInstanceOf(ImplSelfInjectedBean.class, selfInjectedBean.getSelfInjectedBaseClass(), "SelfInjectedBean's self should be an instance of ImplSelfInjectedBean");

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
        Assertions.assertEquals(ranTimes + 3, ClassWrappedMethodWrapper.ranTimes);

        serviceThree.three();
        Assertions.assertTrue(serviceThree.getSentryServiceOne().getClass().getSimpleName().contains("ByteBuddy"));
        Assertions.assertTrue(serviceThree.getSentryServiceTwo().getClass().getSimpleName().contains("ByteBuddy"));
        Assertions.assertTrue(serviceThree.getSentryServiceTwo().getSelf().getClass().getSimpleName().contains("ByteBuddy"));
    }
}
