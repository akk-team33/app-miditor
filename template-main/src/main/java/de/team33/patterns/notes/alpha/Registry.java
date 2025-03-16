package de.team33.patterns.notes.alpha;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Abstracts instances where participants can register {@link Consumer}s as listeners to receive messages
 * that are triggered based on specific events.
 */
public interface Registry<R extends Registry<R>> {

    /**
     * Registers a <em>listener</em> with this registry to receive messages that are triggered based on a specific
     * event.
     * <p>
     * An implementation is expected to be thread-safe!
     *
     * @param channel  The {@link Channel} that represents a specific sort of event,
     *                 causing messages of type {@code <M>}.
     * @param listener A {@link Consumer} to register as a listener.
     * @param <M>      The message type.
     */
    <M> R add(Channel<M> channel, Consumer<? super M> listener);

    /**
     * Unregisters a <em>listener</em> from this registry to no longer receive messages that are triggered
     * based on a specific event.
     * <p>
     * <em>CAUTION</em>: the <em>listener</em> must be identifiable by {@link Object#equals(Object)}
     * to be successfully removed!
     * <p>
     * An implementation is expected to be thread-safe!
     *
     * @param channel  The {@link Channel} that represents a specific sort of event,
     *                 causing messages of type {@code <M>}.
     * @param listener A {@link Consumer} to unregister as a listener.
     * @param <M>      The message type.
     */
    R remove(Channel<?> channel, Consumer<?> listener);

    /**
     * TODO!
     */
    R remove(Collection<? extends Consumer<?>> listeners);

    /**
     * TODO!
     */
    @SuppressWarnings("OverloadedVarargsMethod")
    default R remove(final Consumer<?>... listeners) {
        return remove(Arrays.asList(listeners));
    }
}
