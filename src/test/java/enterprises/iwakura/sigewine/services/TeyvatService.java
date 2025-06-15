package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.annotations.ClassWrapped;
import enterprises.iwakura.sigewine.annotations.OtherAnnotation;
import enterprises.iwakura.sigewine.annotations.Transactional;
import enterprises.iwakura.sigewine.aop.sentry.NoopTransactionConfigurator;
import enterprises.iwakura.sigewine.aop.sentry.SentryTransaction;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.core.utils.collections.TypedArrayList;
import enterprises.iwakura.sigewine.entities.BaseEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@RomaritimeBean
@RequiredArgsConstructor
@ClassWrapped
public class TeyvatService {

    private final PrimordialService primordialService;
    private final SecondWhoCameService secondWhoCameService;
    private final LoggingConfiguration loggingConfiguration; // Will be the info one
    private final BaseDatabaseService databaseService;

    @RomaritimeBean
    private final List<BaseEntity> entities = new TypedArrayList<>(BaseEntity.class);

    @RomaritimeBean
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
}
