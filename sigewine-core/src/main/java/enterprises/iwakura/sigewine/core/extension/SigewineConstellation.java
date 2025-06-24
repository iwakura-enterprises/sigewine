package enterprises.iwakura.sigewine.core.extension;

import enterprises.iwakura.sigewine.core.BeanDefinition;
import enterprises.iwakura.sigewine.core.Sigewine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents an extension for Sigewine, allowing for custom processing of beans.
 */
@Getter
@RequiredArgsConstructor
public abstract class SigewineConstellation {

    /**
     * Priority of the constellation, used to determine the order of processing. Smaller values are processed first.
     */
    protected final int priority;

    /**
     * Processes the beans in the given Sigewine instance.
     *
     * @param sigewine the Sigewine instance containing the beans to process
     */
    public abstract void processBeans(Sigewine sigewine);

    /**
     * Processes a newly created bean instance.
     *
     * @param beanInstance the instance of the bean to process
     * @param beanDefinition the definition of the bean being processed
     * @param sigewine the Sigewine instance that manages the bean
     * @return the processed bean instance, which may be modified or replaced
     */
    public abstract Object processCreatedBeanInstance(Object beanInstance, BeanDefinition beanDefinition, Sigewine sigewine);

}
