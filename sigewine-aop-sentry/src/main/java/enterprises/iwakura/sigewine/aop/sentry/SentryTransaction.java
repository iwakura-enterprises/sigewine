package enterprises.iwakura.sigewine.aop.sentry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for methods or classes that should be treated as Sentry transactions.<br>
 * <b>You must use self-injected bean for calling if you want to call method within the class itself in order for this annotation to take action.</b><br>
 * This is due to the fact that the actual method calls are intercepted only by proxies, not the instance itself.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SentryTransaction {

    String DEFAULT_NAME = "$default-name$";

    /**
     * Name of the transaction.
     * If not specified, it will be set to class name and method name.
     *
     * @return the name of the transaction
     */
    String name() default DEFAULT_NAME;

    /**
     * Operation of the transaction.
     *
     * @return the description of the transaction
     */
    String operation() default "";

    /**
     * Whether to bind the transaction to the current scope.
     * If set to false, the transaction will not be bound to the current scope.
     *
     * @return true if the transaction should be bound to the current scope, false otherwise
     */
    boolean bindToScope() default true;

    /**
     * Whether to create span only if there's an active transaction in current scope.
     *
     * @return true if the transaction should only create a span if there's an active transaction, false otherwise
     */
    boolean onlySpan() default false;

    /**
     * Whether to capture exceptions in the transaction.
     * If set to false, exceptions will not be captured in the transaction.
     *
     * @return true if exceptions should be captured, false otherwise
     */
    boolean captureExceptions() default true;

    /**
     * Configurator class to customize the transaction. This class must have no-arg constructor. Will be called before the transaction is started. Is
     * cached for performance.
     * If not specified, no customization will be applied.
     *
     * @return the configurator class
     */
    Class<? extends TransactionConfigurator> configurator() default NoopTransactionConfigurator.class;

}
