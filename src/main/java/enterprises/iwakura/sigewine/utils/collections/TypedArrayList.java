package enterprises.iwakura.sigewine.utils.collections;

import enterprises.iwakura.sigewine.utils.Preconditions;
import lombok.Getter;

import java.util.ArrayList;

/**
 * A concrete implementation of {@link TypedCollection} that uses an {@link ArrayList} as the underlying data structure.
 *
 * @param <E> The type of elements in this list.
 */
@Getter
public class TypedArrayList<E> extends ArrayList<E> implements TypedCollection<E> {

    /**
     * The class type of the elements in this list.
     */
    private final Class<E> type;

    /**
     * Constructor that initializes the list with the specified type.
     *
     * @param type The class type of the elements in this list.
     */
    public TypedArrayList(Class<E> type) {
        this.type = type;
    }

    /**
     * Adds an object to the array, ensuring it matches the specified type.
     *
     * @param object The object to add to the array.
     *
     * @throws IllegalArgumentException if the object is not of the correct type.
     */
    @Override
    public void addTypedObject(Object object) {
        Preconditions.isOfType(object, type);
        add(type.cast(object));
    }
}
