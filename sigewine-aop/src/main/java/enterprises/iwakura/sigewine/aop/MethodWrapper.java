package enterprises.iwakura.sigewine.aop;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public abstract class MethodWrapper<T extends Annotation> {

    private final Class<T> annotationClass;
    private Map<String, Annotation> annotatedMethodsCache;

    public MethodWrapper(Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    protected abstract void beforeInvocation(Object target, Method method, Object[] args, T annotation, Object proxy);

    protected abstract void afterInvocation(Object target, Method method, Object[] args, T annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy);

    void beforeInvocationInternal(Object target, Method method, Object[] args, Annotation annotation, Object proxy) {
        final var castedAnnotation = castAnnotationObject(annotation);
        beforeInvocation(target, method, args, castedAnnotation, proxy);
    }

    void afterInvocationInternal(Object target, Method method, Object[] args, Annotation annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy) {
        final var castedAnnotation = castAnnotationObject(annotation);
        afterInvocation(target, method, args, castedAnnotation, optionalResult, optionalThrowable, proxy);
    }

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
        if (annotatedMethodsCache != null) {
            return annotatedMethodsCache;
        }

        final var map = new HashMap<String, Annotation>();
        // If class is annotated, add all methods
        if (target.getClass().isAnnotationPresent(annotationClass)) {
            final var classAnnotation = target.getClass().getAnnotation(annotationClass);
            for (Method method : target.getClass().getDeclaredMethods()) {
                map.put(method.getName(), classAnnotation);
            }
        }
        // Go thru all methods and add the ones that are annotated especially
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                map.put(method.getName(), method.getAnnotation(annotationClass));
            }
        }
        annotatedMethodsCache = map;
        return annotatedMethodsCache;
    }
}
