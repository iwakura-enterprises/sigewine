package enterprises.iwakura.sigewine.aop.sentry;

import enterprises.iwakura.sigewine.aop.MethodWrapper;
import io.sentry.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Method wrapper for Sentry transactions.
 * This class handles the creation and management of Sentry transactions and spans based on the {@link SentryTransaction} annotation.
 * It supports binding to the current scope, capturing exceptions, and using custom configurators for transaction options.
 */
@Slf4j
public final class SentryTransactionMethodWrapper extends MethodWrapper<SentryTransaction> {

    /**
     * A thread-local storage for spans that are not bound to the current scope.
     */
    @Getter
    private final static ThreadLocal<ISpan> spanThreadLocal = new ThreadLocal<>();
    private final static Map<Class<? extends TransactionConfigurator>, TransactionConfigurator> configuratorCache = new ConcurrentHashMap<>();

    /**
     * Creates a new instance of SentryMethodWrapper.
     */
    public SentryTransactionMethodWrapper() {
        super(SentryTransaction.class);
    }

    @Override
    protected void beforeInvocation(Object target, Method method, Object[] args, SentryTransaction annotation, Object proxy) {
        final var currentTransaction = Sentry.getCurrentScopes().getTransaction();

        if (annotation.onlySpan() && currentTransaction == null) {
            // If onlySpan is true, we only create a span if there's an active transaction
            return;
        }

        final var txOptions = new TransactionOptions();
        final var configurator = getConfigurator(annotation);
        txOptions.setBindToScope(annotation.bindToScope());
        configurator.configure(annotation, txOptions, target.getClass(), method, args);

        final var spanName = getName(annotation, target.getClass(), method);
        final ISpan span;

        if (currentTransaction == null) {
            // If there's no current transaction, we start a new one
            span = Sentry.startTransaction(spanName, annotation.operation(), txOptions);
            log.debug("Started new transaction: {}", spanName);
        } else {
            // If there's an existing transaction, we create a child span
            span = currentTransaction.startChild(spanName, annotation.operation(), txOptions);
            log.debug("Started child span: {} in transaction: {}", spanName, currentTransaction.getName());
        }

        if (!annotation.bindToScope()) {
            spanThreadLocal.set(span);
        }
    }

    @Override
    protected void afterInvocation(Object target, Method method, Object[] args, SentryTransaction annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy) {
        final ISpan span;

        // Get the span from the current scope or from the thread-local storage in case of non-bind transactions
        if (annotation.bindToScope()) {
            span = Sentry.getSpan();
        } else {
            span = spanThreadLocal.get();
            spanThreadLocal.remove();
        }

        if (span == null) {
            // If there's no span, we do nothing
            log.warn("No span found for transaction. This might happen if the method was called without an active transaction or span.");
            return;
        }

        // If the annotation specifies to capture exceptions, set the throwable on the span
        if (optionalThrowable.isPresent() && annotation.captureExceptions()) {
            span.setThrowable(optionalThrowable.get());
        }

        span.finish(optionalThrowable.isPresent() ? SpanStatus.INTERNAL_ERROR : SpanStatus.OK);

        final String name;
        if (span instanceof SentryTracer sentryTracer) {
            name = sentryTracer.getName();
        } else {
            name = span.getOperation();
        }
        log.debug("Finished span {} with status {}", name, span.getStatus());
    }

    /**
     * Returns transaction configurator for the given annotation. Caches the configurator instances for performance.
     *
     * @param annotation the SentryTransaction annotation
     *
     * @return the TransactionConfigurator instance
     */
    private TransactionConfigurator getConfigurator(SentryTransaction annotation) {
        //@formatter:off
        return configuratorCache.computeIfAbsent(annotation.configurator(), clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception exception) {
                throw new RuntimeException("Failed to instantiate configurator " + clazz.getName() + " (missing no-arg constructor?)", exception);
            }
        });
        //@formatter:on
    }

    /**
     * Returns the name of the transaction based on the annotation and caller class/method.
     *
     * @param annotation   the SentryTransaction annotation
     * @param callerClass  the class where the method is called from
     * @param callerMethod the method where the transaction is called
     *
     * @return the name of the transaction
     */
    private String getName(SentryTransaction annotation, Class<?> callerClass, Method callerMethod) {
        if (!annotation.name().equals(SentryTransaction.DEFAULT_NAME)) {
            return annotation.name();
        }
        return callerClass.getSimpleName() + "#" + callerMethod.getName() + "()";
    }
}
