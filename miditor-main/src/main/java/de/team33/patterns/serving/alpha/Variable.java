package de.team33.patterns.serving.alpha;

/**
 * Represents a service component whose "content" can be determined, redefined
 * and that allows interested parties to subscribe and receive newly emerging "content".
 *
 * @param <C> The type of “content”.
 */
public interface Variable<C> extends Varying<C>, Mutable<C> {
}
