package de.team33.patterns.notes.alpha;

import de.team33.patterns.building.elara.BuilderBase;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class Sender<S extends Sender<S>> extends BuilderBase<S> {

    /**
     * Initializes a new instance and checks the intended <em>sender</em> type for consistency.
     *
     * @param senderClass The {@link Class} representation of the intended effective <em>sender</em> type.
     * @throws IllegalArgumentException if the given <em>sender</em> class does not represent <em>this</em> instance.
     */
    protected Sender(final Class<S> senderClass) {
        super(senderClass);
    }

    /**
     * Returns the associated {@link Audience}.
     */
    protected abstract Audience audience();

    /**
     * Returns a {@link Mapping} to provide messages for certain {@link Channel channels}.
     */
    protected abstract Mapping mapping();

    /**
     * Adds a <em>listener</em> to this <em>sender</em> to receive messages that are fired
     * on a given <em>channel</em>.
     * <p>
     * The <em>listener</em> will receive the first message immediately.
     *
     * @param <M>      The message type.
     * @param channel  The {@link Channel} that represents a specific sort of event,
     *                 causing messages of type {@code <M>}.
     * @param listener A {@link Consumer} to be registered as a <em>listener</em>.
     * @throws IllegalStateException If the {@linkplain #mapping() mapping} does not provide a message
     *                               for at least one of the given <em>channels</em>.
     * @see #addPlain(Channel, Consumer)
     */
    public final <M> S add(final Channel<M> channel, final Consumer<? super M> listener) {
        listener.accept(mapping().get(channel));
        return addPlain(channel, listener);
    }

    /**
     * Adds a <em>listener</em> to this <em>sender</em> to receive messages that are fired
     * on a given <em>channel</em>.
     * <p>
     * The <em>listener</em> will receive the first message the next time the corresponding event
     * actually occurs, usually not immediately.
     *
     * @param <M>      The message type.
     * @param channel  The {@link Channel} that represents a specific sort of event,
     *                 causing messages of type {@code <M>}.
     * @param listener A {@link Consumer} to be registered as a <em>listener</em>.
     * @throws IllegalStateException If the {@linkplain #mapping() mapping} does not provide a message
     *                               for at least one of the given <em>channels</em>.
     * @see #add(Channel, Consumer)
     */
    public final <M> S addPlain(final Channel<M> channel, final Consumer<? super M> listener) {
        audience().add(channel, listener);
        return THIS();
    }

    /**
     * Removes a <em>listener</em> from this <em>sender</em> to no longer receive messages
     * that are fired on a given <em>channel</em>.
     *
     * @param <M>      The message type.
     * @param channel  The {@link Channel} that represents a specific sort of event,
     *                 causing messages of type {@code <M>}.
     * @param listener A {@link Consumer} to be unregistered as a listener.
     */
    public final <M> S remove(final Channel<M> channel, final Consumer<? super M> listener) {
        audience().remove(channel, listener);
        return THIS();
    }

    /**
     * Sends messages provided by the {@linkplain #mapping() associated mapping} to all
     * {@linkplain Consumer listeners} {@linkplain #add(Channel, Consumer) registered}
     * for any of the given <em>channels</em>.
     *
     * @return <em>this</em>.
     * @throws IllegalStateException If the {@linkplain #mapping() mapping} does not provide a message
     *                               for at least one of the given <em>channels</em>.
     */
    protected final S fire(final Channel<?>... channels) {
        return fire(Arrays.asList(channels));
    }

    /**
     * Sends messages provided by the {@linkplain #mapping() associated mapping} to all
     * {@linkplain Consumer listeners} {@linkplain #add(Channel, Consumer) registered}
     * for any of the given <em>channels</em>.
     *
     * @return <em>this</em>.
     * @throws IllegalStateException If the {@linkplain #mapping() mapping} does not provide a message
     *                               for at least one of the given <em>channels</em>.
     */
    protected final S fire(final Iterable<? extends Channel<?>> channels) {
        audience().sendAll(channels, mapping());
        return THIS();
    }
}
