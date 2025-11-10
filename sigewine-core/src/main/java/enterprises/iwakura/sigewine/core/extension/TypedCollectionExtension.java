package enterprises.iwakura.sigewine.core.extension;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import enterprises.iwakura.sigewine.core.BeanDefinition;
import enterprises.iwakura.sigewine.core.Sigewine;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.core.utils.ReflectionUtil;
import enterprises.iwakura.sigewine.core.utils.collections.TypedCollection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * An extension that injects beans into {@link TypedCollection}
 */
@Slf4j
public class TypedCollectionExtension extends SigewineExtension {

    /**
     * Creates a new TypedCollectionExtension with the given priority.
     *
     * @param priority the priority of the extension
     */
    public TypedCollectionExtension(int priority) {
        super(priority);
    }

    @Override
    @SneakyThrows
    public void processBeans(Sigewine sigewine) {
        // Inject beans into collections
        log.debug("Going through beans to inject beans into TypedCollections");
        for (Map.Entry<BeanDefinition, Object> beanEntry : sigewine.getSingletonBeans().entrySet()) {
            final var beanDefinition = beanEntry.getKey();
            // Prefer original bean since it might be a proxy and we need the original instance
            final var bean = Optional.ofNullable(sigewine.getProxiedOriginalBeans().get(beanDefinition)).orElse(beanEntry.getValue());
            log.debug("Going through bean '{}': '{}'", beanDefinition, bean);
            final var declaredFields = ReflectionUtil.getAllFields(bean.getClass());

            for (var field : declaredFields) {
                var annotationPresent = field.isAnnotationPresent(Bean.class);
                var isCollection = Collection.class.isAssignableFrom(field.getType());

                if (annotationPresent && isCollection) {
                    var wasAccessible = field.canAccess(bean);
                    field.setAccessible(true);

                    var collectionObject = field.get(bean);

                    if (!(collectionObject instanceof TypedCollection<?> collection)) {
                        log.warn("Field '{}' in class '{}' is collection, but is not instance of TypedCollection. Ignoring", field.getName(), bean.getClass().getName());
                        continue;
                    }

                    var collectionType = collection.getType();

                    if (collectionType == null) {
                        throw new IllegalArgumentException("Field " + field.getName() + " must have a type");
                    }

                    if (collectionType.isAssignableFrom(TypedCollection.class)) {
                        throw new IllegalArgumentException("Type of TypedCollection " + field.getName() + " cannot be a TypedCollection");
                    }

                    var beansToInject = sigewine.getAllBeansThatAreAssignableFrom(collectionType);
                    log.debug("Injecting {} beans into bean named '{}' for collection named '{}' of type '{}' in class '{}'",
                        beansToInject.size(), beanDefinition, field.getName(), collectionType.getName(), bean.getClass().getName()
                    );
                    beansToInject.forEach(collection::addTypedObject);
                    field.setAccessible(wasAccessible);
                }
            }
        }
    }

    @Override
    public Object processCreatedBeanInstance(Object beanInstance, BeanDefinition beanDefinition, Sigewine sigewine) {
        return beanInstance;
    }
}
