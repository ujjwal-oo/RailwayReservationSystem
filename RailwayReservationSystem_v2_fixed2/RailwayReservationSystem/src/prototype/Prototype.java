package prototype;

/**
 * PROTOTYPE PATTERN - Prototype interface
 * An object that can produce an independent (deep) copy of itself, so new
 * objects can be created by cloning an existing one instead of rebuilding
 * them field by field.
 */
public interface Prototype<T> {
    T copy();
}
