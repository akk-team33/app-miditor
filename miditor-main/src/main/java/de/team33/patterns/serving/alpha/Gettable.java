package de.team33.patterns.serving.alpha;

/**
 * Represents a service component whose "content" can be determined.
 *
 * @param <C> The type of “content”.
 */
@FunctionalInterface
public interface Gettable<C> {

    /**
     * Returns the "content" of <em>this</em> service component.
     */
    C get();
}
