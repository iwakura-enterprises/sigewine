package enterprises.iwakura.sigewine.core.utils;

import java.util.function.Supplier;

import lombok.Data;

@Data
public class BeanAccessor<T> {

    private final Class<T> clazz;
    private final String beanName;
    private Supplier<T> beanSupplier;

    public BeanAccessor(Class<T> clazz) {
        this(clazz, null);
    }

    public BeanAccessor(Class<T> clazz, String beanName) {
        this.clazz = clazz;
        this.beanName = beanName;
    }

    public T getBeanInstance() {
        if (beanSupplier == null) {
            throw new IllegalStateException("Bean supplier is not set for %s%s".formatted(
                clazz.getName(), beanName != null ? " with name '%s'".formatted(beanName) : "")
            );
        }
        return beanSupplier.get();
    }
}
