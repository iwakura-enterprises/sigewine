package enterprises.iwakura.sigewine.utils.collections;

/**
 * A generic interface representing a collection that enforces a specific type for its elements.
 *
 * @param <E> The type of objects that this collection holds.
 */
public interface TypedCollection<E> {

    /**
     * Retrieves the class type of the objects that this collection holds.
     *
     * @return The class type of the objects in this collection.
     */
    Class<E> getType();

    /**
     * Adds an object to the collection. The object must match the type of the collection.
     *
     * @param object The object to add to the collection.
     * @throws IllegalArgumentException if the object is not of the correct type.
     */
    void addTypedObject(Object object);
}
