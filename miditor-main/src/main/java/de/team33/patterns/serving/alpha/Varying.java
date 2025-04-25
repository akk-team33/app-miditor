package de.team33.patterns.serving.alpha;

import java.util.function.Consumer;

/**
 * Represents a service component whose "content" can be determined
 * and that allows interested parties to subscribe and receive newly emerging "content".
 *
 * @param <C> The type of “content”.
 */
public interface Varying<C> extends Gettable<C>, Subscribable<C> {

    /**
     * Quires the current "content" of <em>this</em> service component and subscribes to it for future changes.
     *
     * @param listener A {@link Consumer} that will receive current and newly emerging "content".
     * @return A subscriber id that can be used to {@linkplain #unsubscribe(int) unsubscribe}.
     */
    default int initAndSubscribe(final Consumer<? super C> listener) {
        listener.accept(get());
        return subscribe(listener);
    }
}
