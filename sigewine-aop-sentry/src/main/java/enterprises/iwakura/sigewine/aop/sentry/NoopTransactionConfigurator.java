package enterprises.iwakura.sigewine.aop.sentry;

import io.sentry.TransactionOptions;

import java.lang.reflect.Method;

/**
 * A no-operation implementation of {@link TransactionConfigurator}.
 * This configurator does nothing when the {@link SentryTransaction} annotation is used.
 * It can be used as a default or fallback configurator when no specific configuration is needed.
 */
public final class NoopTransactionConfigurator extends TransactionConfigurator {

    @Override
    public void configure(SentryTransaction annotation, TransactionOptions txOptions, Class<?> callerClass, Method callerMethod, Object[] args) {
        // No operation, this configurator does nothing.
    }
}
