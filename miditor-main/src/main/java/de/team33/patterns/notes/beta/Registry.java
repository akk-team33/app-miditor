package de.team33.patterns.notes.beta;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Abstracts instances where participants can register {@link Consumer}s as listeners to receive messages
 * that are triggered based on specific events.
 */
public interface Registry<R extends Registry<R>> {

    /**
     * Registers a <em>listener</em> with <em>this</em> registry to receive messages
     * transmitted over the given <em>channel</em>.
     * <p>
     * An implementation is expected to be thread-safe!
     *
     * @param <M> The message type.
     * @see #remove(Collection)
     * @see #remove(Consumer[])
     */
    <M> R add(Channel<M> channel, Consumer<? super M> listener);

    /**
     * Unregisters several <em>listeners</em> from <em>this</em> registry to stop receiving messages
     * transmitted over any <em>channel</em>.
     * <p>
     * <em>CAUTION</em>: The <em>listeners</em> must each be identifiable by {@link Object#equals(Object)}
     * to be successfully removed!
     * It is strongly recommended to remove the identical <em>listeners</em> that were added!
     * <p>
     * An implementation is expected to be thread-safe!
     *
     * @see #add(Channel, Consumer)
     * @see #remove(Consumer[])
     */
    R remove(Collection<? extends Consumer<?>> listeners);

    /**
     * Unregisters several <em>listeners</em> from <em>this</em> registry to stop receiving messages
     * transmitted over any <em>channel</em>.
     * <p>
     * <em>CAUTION</em>: The <em>listeners</em> must each be identifiable by {@link Object#equals(Object)}
     * to be successfully removed!
     * It is strongly recommended to remove the identical <em>listeners</em> that were added!
     * <p>
     * An implementation is expected to be thread-safe!
     *
     * @see #add(Channel, Consumer)
     * @see #remove(Collection)
     */
    @SuppressWarnings("OverloadedVarargsMethod")
    default R remove(final Consumer<?>... listeners) {
        return remove(Arrays.asList(listeners));
    }
}
