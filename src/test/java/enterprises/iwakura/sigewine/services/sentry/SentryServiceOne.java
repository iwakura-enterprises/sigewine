package enterprises.iwakura.sigewine.services.sentry;

import enterprises.iwakura.sigewine.aop.sentry.SentryTransaction;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Bean
@RequiredArgsConstructor
public class SentryServiceOne {

    @SneakyThrows
    @SentryTransaction
    public void one() {
        log.info("Executing SentryServiceOne.one()");
        Thread.sleep(10);
    }
}
