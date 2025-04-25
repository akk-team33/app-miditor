package de.team33.patterns.serving.alpha;

/**
 * Represents a service component whose “content” can be redefined.
 *
 * @param <C> The type of “content”.
 */
@FunctionalInterface
public interface Settable<C> {

    /**
     * Redefines the "content" of <em>this</em> service component.
     */
    void set(C content);
}
