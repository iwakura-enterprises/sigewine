package enterprises.iwakura.sigewine.aop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A wrapper for methods that are annotated with a specific annotation.
 * This class provides a way to handle method invocations before and after they are executed,
 * allowing for custom behavior based on the annotation.
 *
 * @param <T> The type of the annotation that this wrapper handles
 */
@Getter
public abstract class MethodWrapper<T extends Annotation> {

    /**
     * The class of the annotation that this wrapper handles.
     */
    private final Class<T> annotationClass;

    /**
     * A cache for annotated methods to avoid repeated reflection lookups.
     */
    private final Map<Class<?>, Map<String, Annotation>> annotatedMethodsCache = new ConcurrentHashMap<>();

    /**
     * Constructor that initializes the wrapper with the specified annotation class.
     *
     * @param annotationClass The class of the annotation that this wrapper handles
     */
    public MethodWrapper(Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Method to be called before the annotated method is invoked.
     *
     * @param target     The target object on which the method is invoked
     * @param method     The method that is being invoked
     * @param args       The arguments passed to the method
     * @param annotation The annotation instance that is present on the method
     * @param proxy      The proxy object that is used to invoke the method
     */
    protected abstract void beforeInvocation(Object target, Method method, Object[] args, T annotation, Object proxy);

    /**
     * Method to be called after the annotated method is invoked. This is called regardless of whether the method
     * throws an exception or not.
     *
     * @param target            The target object on which the method was invoked
     * @param method            Method that was invoked
     * @param args              Arguments passed to the method
     * @param annotation        The annotation instance that is present on the method
     * @param optionalResult    Optional result of the method invocation, if any
     * @param optionalThrowable Optional throwable that was thrown during the method invocation, if any
     * @param proxy             The proxy object that was used to invoke the method
     */
    protected abstract void afterInvocation(Object target, Method method, Object[] args, T annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy);

    /**
     * Internal method to handle the invocation before the annotated method is called.
     * This method casts the annotation object to the specific type and calls the beforeInvocation method.
     *
     * @param target    The target object on which the method is invoked
     * @param method    The method that is being invoked
     * @param args      The arguments passed to the method
     * @param annotation The annotation instance that is present on the method
     * @param proxy     The proxy object that is used to invoke the method
     */
    void beforeInvocationInternal(Object target, Method method, Object[] args, Annotation annotation, Object proxy) {
        final var castedAnnotation = castAnnotationObject(annotation);
        beforeInvocation(target, method, args, castedAnnotation, proxy);
    }

    /**
     * Internal method to handle the invocation after the annotated method is called.
     * This method casts the annotation object to the specific type and calls the afterInvocation method.
     *
     * @param target            The target object on which the method was invoked
     * @param method            Method that was invoked
     * @param args              Arguments passed to the method
     * @param annotation        The annotation instance that is present on the method
     * @param optionalResult    Optional result of the method invocation, if any
     * @param optionalThrowable Optional throwable that was thrown during the method invocation, if any
     * @param proxy             The proxy object that was used to invoke the method
     */
    void afterInvocationInternal(Object target, Method method, Object[] args, Annotation annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy) {
        final var castedAnnotation = castAnnotationObject(annotation);
        afterInvocation(target, method, args, castedAnnotation, optionalResult, optionalThrowable, proxy);
    }

    /**
     * Casts the annotation object to the specific type of annotation that this wrapper handles.
     *
     * @param annotation The annotation object to cast
     *
     * @return The casted annotation object
     *
     * @throws IllegalArgumentException if the annotation is not of the expected type
     */
    private T castAnnotationObject(Object annotation) {
        if (annotationClass.isInstance(annotation)) {
            return annotationClass.cast(annotation);
        }
        throw new IllegalArgumentException("Annotation is not of type " + annotationClass.getName());
    }

    /**
     * Gets all methods that are affected by this method wrapper.
     *
     * @param target The target object to get the methods from
     *
     * @return A map of method names to annotations
     */
    public Map<String, Annotation> getAnnotatedMethods(Object target) {
        final var cachedMethods = annotatedMethodsCache.get(target.getClass());
        if (cachedMethods != null) {
            return cachedMethods;
        }

        final var map = new ConcurrentHashMap<String, Annotation>();
        // If class is annotated, add all methods
        if (target.getClass().isAnnotationPresent(annotationClass)) {
            final var classAnnotation = target.getClass().getAnnotation(annotationClass);
            for (Method method : target.getClass().getDeclaredMethods()) {
                //noinspection DataFlowIssue
                map.put(method.getName(), classAnnotation);
            }
        }
        // Go thru all methods and add the ones that are annotated especially
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                //noinspection DataFlowIssue
                map.put(method.getName(), method.getAnnotation(annotationClass));
            }
        }
        annotatedMethodsCache.put(target.getClass(), map);
        return map;
    }
}
