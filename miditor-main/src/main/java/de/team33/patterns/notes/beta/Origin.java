package de.team33.patterns.notes.beta;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Abstracts instances where participants can register {@link Consumer}s as listeners to receive messages
 * that are triggered based on specific events. In addition to a basic {@link Registry}
 */
public interface Origin extends Registry {

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation delegates to {@link #add(Channel, Mode, Consumer)} using {@link Mode#FEED_INSTANTLY}.
     *
     * @param <M> The message type.
     * @see Mode#FEED_INSTANTLY
     * @see #add(Channel, Mode, Consumer)
     * @see #remove(Channel, Consumer)
     * @see #remove(Collection)
     * @see #remove(Consumer[])
     */
    @Override
    default <M> void add(final Channel<M> channel, final Consumer<? super M> listener) {
        add(channel, Mode.FEED_INSTANTLY, listener);
    }

    /**
     * Registers a <em>listener</em> with <em>this</em> origin to receive messages
     * transmitted over the given <em>channel</em>.
     * <p>
     * The given <em>mode</em> determines whether the <em>listener</em> should instantly receive a message
     * or only when the underlying event occurs next time.
     * <p>
     * An implementation is expected to be thread-safe!
     *
     * @param <M> The message type.
     * @see Mode
     * @see Mode#FEED_INSTANTLY
     * @see Mode#FEED_NEXT_EVENT
     * @see #add(Channel, Consumer)
     * @see #remove(Channel, Consumer)
     * @see #remove(Collection)
     * @see #remove(Consumer[])
     */
    <M> void add(Channel<M> channel, Mode mode, Consumer<? super M> listener);

    /**
     * Determines whether a <em>listener</em> should instantly receive an initial message when
     * {@link #add(Channel, Consumer) added} or only when the underlying event occurs next time.
     */
    enum Mode {

        /**
         * Determines that a <em>listener</em> should instantly receive an initial message when
         * {@link #add(Channel, Mode, Consumer) added} to an {@link Origin}.
         */
        FEED_INSTANTLY,

        /**
         * Determines that a <em>listener</em> should receive a first message
         * only when the underlying event occurs next time.
         */
        FEED_NEXT_EVENT
    }
}
