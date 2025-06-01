package enterprises.iwakura.sigewine;

import enterprises.iwakura.sigewine.annotations.RomaritimeBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BeanDefinition {

    public static final long ABSTRACT_BEAN_PENALTY = 100_000L;

    private final @NonNull String name;
    private final @NonNull Class<?> clazz;
    private final Method method;
    private long beanScore = -1;
    private List<Object> constructorParameters = new ArrayList<>();

    public static BeanDefinition of(Class<?> clazz) {
        return new BeanDefinition(
                Optional.ofNullable(clazz.getAnnotation(RomaritimeBean.class))
                        .map(RomaritimeBean::name)
                        .orElse(""),
                clazz,
                null
        );
    }

    public static BeanDefinition of(Parameter parameter) {
        return new BeanDefinition(
                Optional.ofNullable(parameter.getAnnotation(RomaritimeBean.class))
                        .map(RomaritimeBean::name)
                        .orElse(""),
                parameter.getType(),
                null
        );
    }

    public static BeanDefinition of(Method method) {
        return new BeanDefinition(
                Optional.ofNullable(method.getAnnotation(RomaritimeBean.class))
                        .map(RomaritimeBean::name)
                        .orElse(""),
                method.getReturnType(),
                method
        );
    }

    /**
     * Check if this bean definition is the same as the given one.
     *
     * @param beanDefinition the bean definition to check
     *
     * @return true if this bean definition is the same as the given one, false otherwise
     */
    public boolean is(BeanDefinition beanDefinition) {
        if (beanDefinition == null) {
            return false;
        }

        if (this == beanDefinition) {
            return true;
        }

        if (beanDefinition.hasName()) {
            // Must be the same name
            return name.equals(beanDefinition.getName());
        } else {
            // Specifying non-named bean definition, cannot return named bean definition
            if (hasName()) {
                return false;
            }

            // Check class
            final var beanDefinitionClass = beanDefinition.getClazz();

            if (clazz.equals(beanDefinitionClass)) {
                return true;
            }

            // check if beanDefinition's class is extension of current class
            return beanDefinitionClass.isAssignableFrom(clazz);
        }
    }

    /**
     * Check if this bean definition has a name.
     *
     * @return true if this bean definition has a name, false otherwise
     */
    public boolean hasName() {
        return !name.isBlank();
    }

    /**
     * Compute the number of beans required to create this bean.
     *
     * @return the number of beans required to create this bean
     */
    public long computeBeanScore() {
        if (beanScore != -1) {
            return beanScore;
        }

        // If bean definition is for method, use the method's class
        if (method != null) {
            beanScore = BeanDefinition.of(method.getDeclaringClass()).computeBeanScore();
        } else {
            // Calculate the bean score to create this bean
            var constructors = clazz.getConstructors();

            if (constructors.length == 0) {
                beanScore = 0;
            } else if (constructors.length == 1) {
                var constructor = constructors[0];
                var parameters = constructor.getParameters();

                if (parameters.length == 0) {
                    beanScore = 0;
                } else {
                    // Check if the constructor has a RomaritimeBean annotation
                    long parameterBeans = parameters.length;
                    for (var parameter : parameters) {
                        if (Modifier.isAbstract(parameter.getType().getModifiers()) || parameter.getType().isInterface()) {
                            // Abstract class, add penalty
                            parameterBeans += ABSTRACT_BEAN_PENALTY;
                        }

                        parameterBeans += BeanDefinition.of(parameter).computeBeanScore();
                    }

                    // No RomaritimeBean annotation, return the number of parameters
                    beanScore = parameterBeans;
                }
            } else {
                throw new IllegalArgumentException("Class " + clazz.getName() + " has more than one constructor");
            }
        }

        return beanScore;
    }

    /**
     * Gets list of bean definitions for the constructor of this bean.
     *
     * @return list of bean definitions for the constructor of this bean
     */
    public List<BeanDefinition> getConstructorBeanDefinitions() {
        if (method != null) {
            return List.of();
        }

        var constructors = clazz.getConstructors();

        if (constructors.length == 0) {
            return List.of();
        } else if (constructors.length == 1) {
            var constructor = constructors[0];
            var parameters = constructor.getParameters();

            return Stream.of(parameters)
                         .map(BeanDefinition::of)
                         .toList();
        } else {
            throw new IllegalArgumentException("Class " + clazz.getName() + " has more than one constructor");
        }
    }

    /**
     * Get the constructor parameter types of this bean.
     *
     * @return the constructor parameter types of this bean
     */
    public Class<?>[] getConstructorParameterTypes() {
        return constructorParameters.stream()
                                    .map(Object::getClass)
                                    .toArray(Class<?>[]::new);
    }

    @Override
    public String toString() {
        if (hasName()) {
            return name;
        }

        return clazz.getName();
    }
}
