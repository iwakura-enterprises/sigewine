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
public class SentryServiceThree {

    private final SentryServiceOne sentryServiceOne;
    private final SentryServiceTwo sentryServiceTwo;

    @SneakyThrows
    @SentryTransaction
    public void three() {
        log.info("Executing SentryServiceThree.three()");
        sentryServiceOne.one();
        sentryServiceTwo.two();
        Thread.sleep(10);
    }
}
