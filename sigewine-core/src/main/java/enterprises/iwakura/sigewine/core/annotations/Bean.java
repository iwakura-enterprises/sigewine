package enterprises.iwakura.sigewine.core.annotations;

import enterprises.iwakura.sigewine.core.Sigewine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Annotation to mark elements for the {@link Sigewine} Dependency Injection (DI) framework.<br>
 * 
 * <p>Usage:</p>
 * <ul>
 *   <li><b>Method:</b> Marks the method as a <b>bean provider</b>. The declaring class must have a <b>no-args constructor</b>.</li>
 *   <li><b>Class:</b> Marks the class as a <b>bean</b>. The class must have either a <b>no-args constructor</b> or a constructor accepting other beans. 
 *       <b>Only one constructor is allowed.</b></li>
 *   <li><b>Parameter:</b> Specifies a bean to be injected into the class.</li>
 * <li><b>Field:</b> Injects registered bean into the field.</li>
 * </ul>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Component
public @interface Bean {

    /**
     * Specifies the name of the bean. If not provided, the name defaults to the class name (for classes) or method name (for methods).
     *
     * @return The name of the bean.
     */
    String name() default "";
}
