package enterprises.iwakura.sigewine.core.utils.collections;

import enterprises.iwakura.sigewine.core.utils.Preconditions;
import lombok.Getter;

import java.util.HashSet;

/**
 * A concrete implementation of {@link TypedCollection} that uses a {@link HashSet} as the underlying data structure.
 *
 * @param <E> The type of elements in this set
 */
@Getter
public class TypedHashSet<E> extends HashSet<E> implements TypedCollection<E> {

    /**
     * The class type of the elements in this set.
     */
    private final Class<E> type;

    /**
     * Constructor that initializes the set with the specified type.
     *
     * @param type The class type of the elements in this set.
     */
    public TypedHashSet(Class<E> type) {
        this.type = type;
    }

    /**
     * Adds an object to the set, ensuring it matches the specified type.
     *
     * @param object The object to add to the set.
     *
     * @throws IllegalArgumentException if the object is not of the correct type.
     */
    @Override
    public void addTypedObject(Object object) {
        Preconditions.isOfType(object, type);
        add(type.cast(object));
    }
}
