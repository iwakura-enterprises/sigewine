package enterprises.iwakura.sigewine.aop.extension;

import enterprises.iwakura.sigewine.aop.MethodWrapper;
import enterprises.iwakura.sigewine.core.Sigewine;
import enterprises.iwakura.sigewine.aop.SigewineInvocationHandler;
import enterprises.iwakura.sigewine.core.extension.SigewineConstellation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * AOP extension for Sigewine that creates proxies for beans with annotated methods.
 */
@Slf4j
public class AopConstellation extends SigewineConstellation {

    /**
     * ByteBuddy instance for creating proxies.
     */
    protected final ByteBuddy byteBuddy = new ByteBuddy();

    /**
     * Map of method wrappers for different annotations.
     * The key is the annotation class, and the value is the method wrapper for that annotation.
     */
    protected final Map<Class<? extends Annotation>, MethodWrapper<? extends Annotation>> methodWrapperMap = new HashMap<>();

    /**
     * Creates a new AopConstellation with specified priority.
     *
     * @param priority Priority of the constellation, lower values are processed first
     */
    public AopConstellation(int priority) {
        super(priority);
    }

    /**
     * Adds a method wrapper to the method wrapper map.
     *
     * @param methodWrapper Method wrapper to add
     */
    public void addMethodWrapper(MethodWrapper<?> methodWrapper) {
        methodWrapperMap.put(methodWrapper.getAnnotationClass(), methodWrapper);
    }

    @SneakyThrows
    @Override
    public void processBeans(Sigewine sigewine) {
        final var singletonBeans = sigewine.getSingletonBeans();

        log.debug("Creating proxies for beans...");
        for (var entry : singletonBeans.entrySet()) {
            final var beanDefinition = entry.getKey();
            final var originalBean = entry.getValue();
            final var methodWrappers = getMethodWrappersForObject(originalBean, methodWrapperMap);

            if (!methodWrappers.isEmpty()) {
                // Add the original bean to the map
                sigewine.getProxiedOriginalBeans().put(beanDefinition, originalBean);
                var beanToProxy = originalBean;

                log.debug("Creating proxy for bean '{}': '{}'", beanDefinition, methodWrappers);

                final var sigewineProxy = new SigewineInvocationHandler(methodWrappers, beanToProxy);

                beanToProxy = byteBuddy
                    .subclass(beanToProxy.getClass())
                    .method(ElementMatchers.any()) // Match all methods since proxied bean does not have the methods annotated anymore
                    .intercept(InvocationHandlerAdapter.of(sigewineProxy))
                    .make()
                    .load(beanToProxy.getClass().getClassLoader())
                    .getLoaded()
                    .getConstructors()[0] // We have already checked that the class has a constructor
                    .newInstance(beanDefinition.getConstructorParameters().toArray());
                singletonBeans.put(beanDefinition, beanToProxy);
            }
        }
        log.debug("Created '{}' bean proxies", sigewine.getProxiedOriginalBeans().size());
    }

    /**
     * Returns a list of method wrappers for the given object that should be used.
     *
     * @param bean Object to get the wrappers for
     * @param methodWrapperMap Map of method wrappers to use
     *
     * @return List of method wrappers for the object
     */
    protected Collection<MethodWrapper<?>> getMethodWrappersForObject(
            Object bean,
            Map<Class<? extends Annotation>, MethodWrapper<? extends Annotation>> methodWrapperMap
    ) {
        final var methodWrappers = new HashSet<MethodWrapper<?>>();

        // Get all annotations from the class and methods
        final var annotations = new ArrayList<>(List.of(bean.getClass().getAnnotations()));
        for (Method declaredMethod : bean.getClass().getDeclaredMethods()) {
            Collections.addAll(annotations, declaredMethod.getAnnotations());
        }

        for (var annotation : annotations) {
            final var methodWrapper = methodWrapperMap.get(annotation.annotationType());
            if (methodWrapper != null) {
                methodWrappers.add(methodWrapper);
            }
        }

        return methodWrappers;
    }
}
