package enterprises.iwakura.sigewine.services.sentry;

import enterprises.iwakura.sigewine.aop.sentry.SentryTransaction;
import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RomaritimeBean
@RequiredArgsConstructor
public class SentryServiceTwo {

    @RomaritimeBean
    private SentryServiceTwo self;

    @SneakyThrows
    @SentryTransaction
    public void two() {
        log.info("Executing SentryServiceTwo.two()");
        Thread.sleep(10);
        self.twoWithSelf();
    }

    @SneakyThrows
    @SentryTransaction
    public void twoWithSelf() {
        log.info("Executing SentryServiceTwo.twoWithSelf()");
        Thread.sleep(10);
    }
}
