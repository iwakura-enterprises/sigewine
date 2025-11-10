package enterprises.iwakura.sigewine.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import lombok.experimental.UtilityClass;

/**
 * Utility class for reflection-related operations.
 */
@UtilityClass
public class ReflectionUtil {

    /**
     * Gets all fields of the class and its superclasses.
     *
     * @param clazz Class to get the fields from
     *
     * @return Array of fields
     */
    public static Field[] getAllFields(Class<?> clazz) {
        final var fields = new ArrayList<Field>();
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields.toArray(new Field[0]);
    }
}
