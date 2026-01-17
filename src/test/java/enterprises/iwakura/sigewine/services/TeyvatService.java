package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.annotations.ClassWrapped;
import enterprises.iwakura.sigewine.annotations.OtherAnnotation;
import enterprises.iwakura.sigewine.annotations.Transactional;
import enterprises.iwakura.sigewine.aop.sentry.SentryTransaction;
import enterprises.iwakura.sigewine.beans.HeavenlyPrinciplesBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import enterprises.iwakura.sigewine.core.Sigewine;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.entities.BaseEntity;
import enterprises.iwakura.sigewine.extension.ExternalClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Bean
@RequiredArgsConstructor
@ClassWrapped
public class TeyvatService {

    private final PrimordialService primordialService;
    private final SecondWhoCameService secondWhoCameService;
    private final LoggingConfiguration loggingConfiguration; // Will be the info one
    private final BaseDatabaseService databaseService;
    private final HeavenlyPrinciplesBean heavenlyPrinciplesBean;
    private final Sigewine sigewine;
    private final ExternalClass externalClass;
    private final InsideTeyvatClass insideTeyvatClass;

    private final List<BaseEntity> entities;

    @Bean
    private TeyvatService self;

    @OtherAnnotation
    @Transactional
    @SentryTransaction
    public void someAnnotatedMethod() {
        log.info("This is an annotated method");
        self.innerMethod();
    }

    @SentryTransaction(name = "someCustomName", operation = "customOperation")
    public void someUnannotatedMethod() {
        log.info("This is an unannotated method");
    }

    @SentryTransaction(name = "innerMethodName", operation = "Super duper operation")
    public void innerMethod() {
        log.info("This is an inner method");
    }

    @Bean
    @Getter
    @RequiredArgsConstructor
    public static class InsideTeyvatClass {

        private final ExternalClass externalClass;
    }
}
