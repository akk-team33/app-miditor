package de.team33.patterns.serving.alpha;

import java.util.function.Consumer;

/**
 * Represents a service component that allows interested parties to subscribe
 * and receive newly emerging "content".
 *
 * @param <C> The type of “content”.
 */
public interface Subscribable<C> {

    /**
     * Subscribes <em>this</em> service component for newly emerging "content".
     *
     * @param listener A {@link Consumer} that will receive newly emerging "content".
     * @return A subscriber id that can be used to {@linkplain #unsubscribe(int) unsubscribe}.
     */
    int subscribe(Consumer<? super C> listener);

    /**
     * Unsubscribes <em>this</em> service component.
     *
     * @param subscriberId The subscriber id obtained by {@link #subscribe(Consumer)}.
     */
    void unsubscribe(int subscriberId);
}
