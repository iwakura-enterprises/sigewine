package enterprises.iwakura.sigewine.services.sentry;

import enterprises.iwakura.sigewine.aop.sentry.SentryTransaction;
import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RomaritimeBean
@RequiredArgsConstructor
public class SentryServiceOne {

    @SneakyThrows
    @SentryTransaction
    public void one() {
        log.info("Executing SentryServiceOne.one()");
        Thread.sleep(10);
    }
}
