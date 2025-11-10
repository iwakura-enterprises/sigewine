package enterprises.iwakura.sigewine.core.extension;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import enterprises.iwakura.sigewine.core.BeanDefinition;
import enterprises.iwakura.sigewine.core.Sigewine;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.core.utils.BeanAccessor;
import enterprises.iwakura.sigewine.core.utils.ReflectionUtil;
import enterprises.iwakura.sigewine.core.utils.collections.TypedCollection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Built-in extension that injects beans into fields annotated with {@link Bean}, excluding collections.
 */
@Slf4j
public class InjectBeanExtension extends SigewineExtension {

    public InjectBeanExtension(int priority) {
        super(priority);
    }

    @Override
    @SneakyThrows
    public void processBeans(Sigewine sigewine) {
        log.debug("Going through beans to inject itself");
        for (Map.Entry<BeanDefinition, Object> beanEntry : sigewine.getSingletonBeans().entrySet()) {
            final var beanDefinition = beanEntry.getKey();
            final var bean = Optional.ofNullable(sigewine.getProxiedOriginalBeans().get(beanDefinition))
                .orElse(beanEntry.getValue());
            log.debug("Going through bean '{}': '{}'", beanDefinition, bean);
            final var declaredFields = ReflectionUtil.getAllFields(bean.getClass());

            for (var field : declaredFields) {
                var annotationPresent = field.isAnnotationPresent(Bean.class);
                var isCollection = Collection.class.isAssignableFrom(field.getType());
                var isBeanAccessor = BeanAccessor.class.isAssignableFrom(field.getType());

                if (annotationPresent && !isCollection) {
                    if (isBeanAccessor) {
                        injectBeanAccessor(sigewine, field, bean);
                    } else {
                        injectBean(sigewine, field, bean);
                    }
                }
            }
        }
    }

    @SneakyThrows
    private static void injectBean(Sigewine sigewine, Field field, Object bean) {
        var specifiedBeanName = Optional.ofNullable(field.getAnnotation(Bean.class).name()).orElse("");
        log.debug("Injecting bean into field '{}' of class '{}' with name '{}' in class '{}'",
            field.getName(), field.getType(), specifiedBeanName, bean.getClass().getName()
        );
        var injectedBean = sigewine.inject(field.getType(), specifiedBeanName);
        var wasAccessible = field.canAccess(bean);
        field.setAccessible(true);
        field.set(bean, injectedBean);
        field.setAccessible(wasAccessible);
    }

    @SneakyThrows
    private void injectBeanAccessor(Sigewine sigewine, Field field, Object bean) {
        var wasAccessible = field.canAccess(bean);
        field.setAccessible(true);
        var beanAccessor = (BeanAccessor<?>) field.get(bean);

        if (beanAccessor == null) {
            throw new IllegalArgumentException(
                "Field %s of BeanAccessor in class %s must not be null if annotated with @Bean".formatted(
                    field.getName(), bean.getClass()));
        }

        log.debug("Injecting BeanAccessor into field '{}' of class '{}' for bean type '{}' with name '{}' in class '{}'",
            field.getName(), field.getType(), beanAccessor.getClazz(), beanAccessor.getBeanName(), bean.getClass().getName()
        );
        //noinspection rawtypes,unchecked
        beanAccessor.setBeanSupplier((Supplier)() -> {
            return sigewine.inject(beanAccessor.getClazz(), beanAccessor.getBeanName());
        });
        field.setAccessible(wasAccessible);
    }

    @Override
    public Object processCreatedBeanInstance(Object beanInstance, BeanDefinition beanDefinition, Sigewine sigewine) {
        return beanInstance;
    }
}
