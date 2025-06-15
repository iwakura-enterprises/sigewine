package enterprises.iwakura.sigewine.aop.sentry;

import io.sentry.TransactionOptions;

import java.lang.reflect.Method;

/**
 * Abstract class for configuring Sentry transactions.
 * This class should be extended to provide custom transaction configurations.
 * The {@link #configure(SentryTransaction, TransactionOptions, Class, Method, Object[])} method must be implemented to apply the desired
 * configuration.
 */
public abstract class TransactionConfigurator {

    /**
     * Configures the transaction options based on the provided annotation and method context.
     *
     * @param annotation   the SentryTransaction annotation
     * @param txOptions    the TransactionOptions to configure. If there's a transaction in the current scope, this should be treated this as a
     *                     {@link io.sentry.SpanOptions}.
     * @param callerClass  the class where the method is called from
     * @param callerMethod the method where the transaction is called
     * @param args         the arguments passed to the method
     */
    public abstract void configure(SentryTransaction annotation, TransactionOptions txOptions, Class<?> callerClass, Method callerMethod, Object[] args);

}
