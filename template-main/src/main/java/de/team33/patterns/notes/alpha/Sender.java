package de.team33.patterns.notes.alpha;

import de.team33.patterns.building.elara.BuilderBase;

import java.util.function.Consumer;

public abstract class Sender<S extends Sender<S>> extends BuilderBase<S> {

    private final Audience audience;

    /**
     * Initializes a new instance and checks the intended <em>sender</em> type for consistency.
     *
     * @param senderClass The {@link Class} representation of the intended effective <em>sender</em> type.
     * @throws IllegalArgumentException if the given <em>sender</em> class does not represent <em>this</em> instance.
     */
    protected Sender(final Class<S> senderClass) {
        this(senderClass, new Audience());
    }

    /**
     * Initializes a new instance and checks the intended <em>sender</em> type for consistency.
     *
     * @param senderClass The {@link Class} representation of the intended effective <em>sender</em> type.
     * @param audience    An {@link Audience} to be used. Typically <em>new</em>.
     *                    Be careful sharing an existing {@link Audience}!
     * @throws IllegalArgumentException if the given <em>sender</em> class does not represent <em>this</em> instance.
     */
    protected Sender(final Class<S> senderClass, final Audience audience) {
        super(senderClass);
        this.audience = audience;
    }

    /**
     * Returns the associated {@link Audience}.
     */
    protected final Audience audience() {
        return audience;
    }

    /**
     * Adds a <em>listener</em> to this <em>sender</em> to receive messages that are triggered based on a specific
     * event.
     *
     * @param <M>      The message type.
     * @param channel  The {@link Channel} that represents a specific sort of event,
     *                 causing messages of type {@code <M>}.
     * @param listener A {@link Consumer} to be registered as a <em>listener</em>.
     */
    public final <M> S addListener(final Channel<M> channel, final Consumer<? super M> listener) {
        audience().add(channel, listener);
        return THIS();
    }

    /**
     * Sends a given message to all listeners that have {@linkplain #addListener(Channel, Consumer) registered}
     * for the given {@link Channel}.
     *
     * @param <M> The message type.
     */
    protected final <M> S sendMessage(final Channel<M> channel, final M message) {
        audience().send(channel, message);
        return THIS();
    }
}
