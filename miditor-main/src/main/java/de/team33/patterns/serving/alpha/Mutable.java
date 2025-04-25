package de.team33.patterns.serving.alpha;

/**
 * Represents a service component whose "content" can be determined and redefined.
 *
 * @param <C> The type of “content”.
 */
public interface Mutable<C> extends Gettable<C>, Settable<C> {
}
