package enterprises.iwakura.sigewine.annotations;

import enterprises.iwakura.sigewine.Sigewine;
import enterprises.iwakura.sigewine.utils.collections.TypedCollection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark elements for the {@link Sigewine} Dependency Injection (DI) framework.<br>
 * 
 * <p>Usage:</p>
 * <ul>
 *   <li><b>Method:</b> Marks the method as a <b>bean provider</b>. The declaring class must have a <b>no-args constructor</b>.</li>
 *   <li><b>Class:</b> Marks the class as a <b>bean</b>. The class must have either a <b>no-args constructor</b> or a constructor accepting other beans. 
 *       <b>Only one constructor is allowed.</b></li>
 *   <li><b>Parameter:</b> Specifies a bean to be injected into the class.</li>
 *   <li><b>Field:</b> Specifies a {@link TypedCollection}. Beans will be injected based on the
 *       <code>TypedCollection</code>'s type.</li>
 * </ul>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface RomaritimeBean {

    /**
     * Specifies the name of the bean. If not provided, the name defaults to the class name (for classes) or method name (for methods).
     *
     * @return The name of the bean.
     */
    String name() default "";
}
