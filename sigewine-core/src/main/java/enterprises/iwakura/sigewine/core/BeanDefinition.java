package enterprises.iwakura.sigewine.core;

import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a bean definition. Holds information about the bean's name, class, method, and constructor parameters.<br>
 * Holds so called "bean score" which is the number of beans required to create this bean. If the bean is abstract or an interface, it will add a penalty to the score.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BeanDefinition {

    /**
     * Penalty for abstract beans, interfaces or classes without RomaritimeBean annotation.
     */
    public static final long BEAN_SCORE_PENALTY = 100_000L;

    /**
     * The name of the bean. If the bean is not named, this will be an empty string.
     */
    private final @NonNull String name;

    /**
     * The class of the bean. This is the class that will be instantiated when the bean is created.
     */
    private final @NonNull Class<?> clazz;

    /**
     * The method that defines this bean, if applicable. This is used for method-based beans.
     */
    private final Method method;

    /**
     * The score of the bean. This is the number of beans required to create this bean.
     */
    private long beanScore = -1;

    /**
     * The parameters of the constructor of this bean. This is used to create the bean.
     */
    private List<Object> constructorParameters = new ArrayList<>();

    /**
     * Create a new bean definition.
     *
     * @param clazz the class of the bean
     *
     * @return a new bean definition for the given class
     */
    public static BeanDefinition of(Class<?> clazz) {
        return new BeanDefinition(
                Optional.ofNullable(clazz.getAnnotation(RomaritimeBean.class))
                        .map(RomaritimeBean::name)
                        .orElse(""),
                clazz,
                null
        );
    }

    /**
     * Create a new bean definition from a method parameter.
     *
     * @param parameter the method parameter to create the bean definition from
     *
     * @return a new bean definition for the given method parameter
     */
    public static BeanDefinition of(Parameter parameter) {
        return new BeanDefinition(
                Optional.ofNullable(parameter.getAnnotation(RomaritimeBean.class))
                        .map(RomaritimeBean::name)
                        .orElse(""),
                parameter.getType(),
                null
        );
    }

    /**
     * Create a new bean definition from a method.
     *
     * @param method the method to create the bean definition from
     *
     * @return a new bean definition for the given method
     */
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
    public long computeBeanScore(Set<BeanDefinition> otherBeanDefinitions) {
        if (beanScore != -1) {
            return beanScore;
        }

        // If bean definition is for method, use the method's class
        if (method != null) {
            beanScore = BeanDefinition.of(method.getDeclaringClass()).computeBeanScore(otherBeanDefinitions);
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
                            parameterBeans += BEAN_SCORE_PENALTY;
                        }

                        final var parameterClass = parameter.getType();
                        if (!parameterClass.isAnnotationPresent(RomaritimeBean.class)) {
                            parameterBeans += BEAN_SCORE_PENALTY;
                            parameterBeans += sumBeanScoresOfRelatedBeanDefinitions(parameterClass, otherBeanDefinitions);
                        } else {
                            parameterBeans += BeanDefinition.of(parameter).computeBeanScore(otherBeanDefinitions);
                        }
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

    /**
     * Sums the bean scores of all bean definitions that are related to the given class.
     *
     * @param clazz           the class to check for related bean definitions
     * @param beanDefinitions the set of bean definitions to check
     *
     * @return the sum of bean scores of all related bean definitions
     */
    private static long sumBeanScoresOfRelatedBeanDefinitions(Class<?> clazz, Set<BeanDefinition> beanDefinitions) {
        return beanDefinitions.stream()
                              .filter(beanDefinition -> beanDefinition.is(BeanDefinition.of(clazz)))
                              .mapToLong(BeanDefinition::getBeanScore)
                              .sum();
    }
}
