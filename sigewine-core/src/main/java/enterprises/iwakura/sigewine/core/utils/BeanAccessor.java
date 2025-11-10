package enterprises.iwakura.sigewine.core.utils;

import java.util.function.Supplier;

import enterprises.iwakura.sigewine.core.extension.InjectBeanExtension;
import lombok.Data;

/**
 * Bean accessor to lazily retrieve bean instances.
 *
 * @param <T> the type of the bean
 */
@Data
public class BeanAccessor<T> {

    private final Class<T> clazz;
    private final String beanName;
    private Supplier<T> beanSupplier;

    /**
     * Creates a BeanAccessor for the given class.
     *
     * @param clazz the class of the bean
     */
    public BeanAccessor(Class<T> clazz) {
        this(clazz, null);
    }

    /**
     * Creates a BeanAccessor for the given class and bean name.
     *
     * @param clazz    the class of the bean
     * @param beanName the name of the bean
     */
    public BeanAccessor(Class<T> clazz, String beanName) {
        this.clazz = clazz;
        this.beanName = beanName;
    }

    /**
     * Retrieves the bean instance using the supplier (that should be set by {@link InjectBeanExtension})
     *
     * @return the bean instance
     */
    public T getBeanInstance() {
        if (beanSupplier == null) {
            throw new IllegalStateException("Bean supplier is not set for %s%s".formatted(
                clazz.getName(), beanName != null ? " with name '%s'".formatted(beanName) : "")
            );
        }
        return beanSupplier.get();
    }
}
