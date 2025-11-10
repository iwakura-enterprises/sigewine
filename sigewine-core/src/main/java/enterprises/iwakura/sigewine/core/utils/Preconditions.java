package enterprises.iwakura.sigewine.core.utils;

import enterprises.iwakura.sigewine.core.annotations.Bean;

import java.lang.reflect.Method;

/**
 * Utility class providing common validation methods for the Sigewine framework.
 * <p>
 * This class contains static methods to validate various conditions, such as
 * the presence of constructors, annotations, and method return types.
 * </p>
 */
public final class Preconditions {

    /**
     * Private constructor to prevent instantiation.
     */
    private Preconditions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates that the class has a no-argument constructor.
     *
     * @param clazz The class to validate.
     * @throws IllegalArgumentException if the class does not have a no-argument constructor.
     */
    public static void checkNoArgConstructor(Class<?> clazz) {
        if (clazz.getConstructors().length == 0) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " has no constructor");
        } else {
            boolean hasNoArgsConstructor = false;

            for (var constructor : clazz.getConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    hasNoArgsConstructor = true;
                    break;
                }
            }

            if (!hasNoArgsConstructor) {
                throw new IllegalArgumentException("Class " + clazz.getName() + " has no no-arg constructor");
            }
        }
    }

    /***
     * Checks if the class has exactly one constructor.
     * @param clazz Class to check
     * @throws IllegalArgumentException if the class does not have exactly one constructor
     */
    public static void checkOneConstructor(Class<?> clazz) {
        if (clazz.getConstructors().length != 1) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " must have exactly one constructor");
        }
    }

    /**
     * Checks if the method has a void return type.
     *
     * @param method Method to check
     *
     * @throws IllegalArgumentException if the method has a void return type
     */
    public static void checkNoVoidReturnType(Method method) {
        if (method.getReturnType() == Void.TYPE) {
            throw new IllegalArgumentException("Method " + method + " cannot have a void return type");
        }
    }

    /**
     * Checks if the method has a primitive return type.
     *
     * @param method Method to check
     *
     * @throws IllegalArgumentException if the method has a primitive return type
     */
    public static void checkNoPrimitiveReturnType(Method method) {
        if (method.getReturnType().isPrimitive()) {
            throw new IllegalArgumentException("Method " + method + " cannot have a primitive return type");
        }
    }

    /**
     * Checks if the class is annotated with the specified annotation.
     *
     * @param clazz           Class to check
     * @param romaritimeClass Annotation class to check
     */
    public static void checkAnnotated(Class<?> clazz, Class<Bean> romaritimeClass) {
        if (!clazz.isAnnotationPresent(romaritimeClass)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with " + romaritimeClass.getName());
        }
    }

    /**
     * Checks if the class is annotated with the specified annotation.
     *
     * @param object Object to check
     * @param type   Class to check
     * @param <E>    Type of the class
     */
    public static <E> void isOfType(Object object, Class<E> type) {
        if (!type.isInstance(object)) {
            throw new IllegalArgumentException("Object " + object + " is not of type " + type.getName());
        }
    }
}
