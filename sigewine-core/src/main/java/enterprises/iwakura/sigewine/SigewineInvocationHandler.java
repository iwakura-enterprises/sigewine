package enterprises.iwakura.sigewine;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Invocation handler for Sigewine that handles method invocations and applies
 * the appropriate method wrappers based on annotations.
 */
@RequiredArgsConstructor
public final class SigewineInvocationHandler implements InvocationHandler {

    private final List<MethodWrapper<? extends Annotation>> methodWrappers;
    private final Object target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Prep
        Optional<Object> optionalResult = Optional.empty();
        Optional<Throwable> optionalThrowable = Optional.empty();

        // Before invocation
        for (final var methodWrapper : methodWrappers) {
            final var annotatedMethods = methodWrapper.getAnnotatedMethods(target);

            if (!annotatedMethods.isEmpty()) {
                final var annotation = annotatedMethods.get(method.getName());
                if (annotation != null) {
                    // Run beforeInvocation for each method wrapper
                    methodWrapper.beforeInvocationInternal(target, method, args, annotation, proxy);
                }
            }
        }
        // Invocation
        try {
            optionalResult = Optional.ofNullable(method.invoke(target, args));
        } catch (Throwable throwable) {
            optionalThrowable = Optional.of(throwable);
        }

        // After invocation
        for (final var methodWrapper : methodWrappers) {
            final var annotatedMethods = methodWrapper.getAnnotatedMethods(target);

            if (!annotatedMethods.isEmpty()) {
                final var annotation = annotatedMethods.get(method.getName());
                if (annotation != null) {
                    // Run afterInvocation for each method wrapper
                    methodWrapper.afterInvocationInternal(
                            target,
                            method,
                            args,
                            annotation,
                            optionalResult,
                            optionalThrowable,
                            proxy
                    );
                }
            }
        }

        // Re-throw the exception to allow it to propagate
        if (optionalThrowable.isPresent()) {
            throw optionalThrowable.get();
        }

        // Return the result or null if no result
        return optionalResult.orElse(null);
    }
}
